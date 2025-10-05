package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "BlockTarget", identifier = "A", description = "Detects perfect block center targeting characteristic of bots", checkType = CheckType.PLACE)
public class BlockTargetA extends Check {
    private final List<TargetData> recentTargets = new ArrayList<>();

    public BlockTargetA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        PacketDatas packetDatas = playerData.packetDataList();
        
        if (packetDatas.size(CheckType.PLACE) == 0) {
            return;
        }

        PacketData placePacket = packetDatas.getLatest(CheckType.PLACE);
        if (placePacket == null) {
            return;
        }

        // Get rotation and position at time of placement
        PacketData rotation = packetDatas.getLatest(CheckType.ROTATION);
        PacketData flying = packetDatas.getLatest(CheckType.FLYING);
        
        PacketData rotationData = null;
        PacketData positionData = null;
        
        if (rotation != null && flying != null) {
            rotationData = rotation.timeStamp() > flying.timeStamp() ? rotation : flying;
            positionData = rotationData;
        } else if (rotation != null) {
            rotationData = rotation;
            positionData = rotation;
        } else if (flying != null) {
            rotationData = flying;
            positionData = flying;
        }

        if (rotationData == null || rotationData.rotationData() == null || 
            positionData == null || positionData.positionData() == null) {
            return;
        }

        RotationData rot = rotationData.rotationData();
        PositionData pos = positionData.positionData();

        // Calculate where the player is looking
        Vector lookVec = getVectorForRotation(rot.pitch(), rot.yaw());
        
        // Estimate target block position (simplified raytrace)
        // In reality, this would need proper raytracing, but we can detect patterns
        double reachDistance = 5.0;
        double targetX = pos.x() + lookVec.getX() * reachDistance;
        double targetY = pos.y() + lookVec.getY() * reachDistance + 1.62; // Eye height
        double targetZ = pos.z() + lookVec.getZ() * reachDistance;

        // Check how close to block center the target is
        double xOffset = Math.abs((targetX % 1.0) - 0.5);
        double yOffset = Math.abs((targetY % 1.0) - 0.5);
        double zOffset = Math.abs((targetZ % 1.0) - 0.5);

        TargetData target = new TargetData(xOffset, yOffset, zOffset);
        recentTargets.add(target);

        if (recentTargets.size() > 12) {
            recentTargets.remove(0);
        }

        if (recentTargets.size() < 8) {
            return;
        }

        // Analyze targeting patterns
        
        // 1. Check for perfect center targeting (bots often aim at exact block centers)
        int perfectCenters = 0;
        for (TargetData t : recentTargets) {
            if (t.isPerfectCenter()) {
                perfectCenters++;
            }
        }
        
        boolean tooManyPerfectCenters = perfectCenters > recentTargets.size() * 0.6;

        // 2. Check for consistent offset patterns (same targeting algorithm)
        double xVariance = calculateOffsetVariance(recentTargets, 0);
        double yVariance = calculateOffsetVariance(recentTargets, 1);
        double zVariance = calculateOffsetVariance(recentTargets, 2);
        
        boolean consistentPattern = xVariance < 0.01 && zVariance < 0.01;

        // 3. Check for exact repetition (same offset used repeatedly)
        int exactRepeats = 0;
        for (int i = 0; i < recentTargets.size() - 1; i++) {
            for (int j = i + 1; j < recentTargets.size(); j++) {
                if (recentTargets.get(i).isSimilarTo(recentTargets.get(j))) {
                    exactRepeats++;
                }
            }
        }
        
        boolean manyRepeats = exactRepeats > (recentTargets.size() * (recentTargets.size() - 1) / 2) * 0.4;

        debug("Perfect centers: " + perfectCenters + " X variance: " + xVariance + 
              " Z variance: " + zVariance + " Repeats: " + exactRepeats);

        int violations = 0;
        if (tooManyPerfectCenters) violations++;
        if (consistentPattern) violations++;
        if (manyRepeats) violations++;

        if (violations >= 2) {
            increaseVl(violations);
        }
    }

    private Vector getVectorForRotation(float pitch, float yaw) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        
        return new Vector(x, y, z);
    }

    private double calculateOffsetVariance(List<TargetData> targets, int axis) {
        List<Double> offsets = new ArrayList<>();
        for (TargetData t : targets) {
            switch (axis) {
                case 0: offsets.add(t.xOffset); break;
                case 1: offsets.add(t.yOffset); break;
                case 2: offsets.add(t.zOffset); break;
            }
        }
        
        if (offsets.isEmpty()) return 0;
        
        double sum = 0;
        for (double val : offsets) {
            sum += val;
        }
        double mean = sum / offsets.size();
        
        double variance = 0;
        for (double val : offsets) {
            variance += Math.pow(val - mean, 2);
        }
        
        return variance / offsets.size();
    }

    private static class TargetData {
        final double xOffset, yOffset, zOffset;
        
        TargetData(double xOffset, double yOffset, double zOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
        }
        
        boolean isPerfectCenter() {
            return xOffset < 0.05 && yOffset < 0.05 && zOffset < 0.05;
        }
        
        boolean isSimilarTo(TargetData other) {
            return Math.abs(this.xOffset - other.xOffset) < 0.02 &&
                   Math.abs(this.yOffset - other.yOffset) < 0.02 &&
                   Math.abs(this.zOffset - other.zOffset) < 0.02;
        }
    }
}

