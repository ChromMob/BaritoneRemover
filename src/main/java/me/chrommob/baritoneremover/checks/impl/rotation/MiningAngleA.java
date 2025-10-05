package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "MiningAngle", identifier = "A", description = "Detects consistent mining pitch angles characteristic of Baritone", checkType = CheckType.MINING)
public class MiningAngleA extends Check {
    private final List<Float> miningPitches = new ArrayList<>();
    private final List<Float> miningYaws = new ArrayList<>();

    public MiningAngleA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        PacketDatas packetDatas = playerData.packetDataList();
        
        if (packetDatas.size(CheckType.MINING) == 0) {
            return;
        }

        PacketData miningPacket = packetDatas.getLatest(CheckType.MINING);
        if (miningPacket == null) {
            return;
        }

        // Get the rotation when mining started
        PacketData rotation = packetDatas.getLatest(CheckType.ROTATION);
        PacketData flying = packetDatas.getLatest(CheckType.FLYING);
        
        PacketData rotationData = null;
        if (rotation != null && flying != null) {
            rotationData = rotation.timeStamp() > flying.timeStamp() ? rotation : flying;
        } else if (rotation != null) {
            rotationData = rotation;
        } else if (flying != null) {
            rotationData = flying;
        }

        if (rotationData == null || rotationData.rotationData() == null) {
            return;
        }

        float pitch = rotationData.rotationData().pitch();
        float yaw = rotationData.rotationData().yaw();

        miningPitches.add(pitch);
        miningYaws.add(yaw);

        // Keep last 15 mining attempts
        if (miningPitches.size() > 15) {
            miningPitches.remove(0);
            miningYaws.remove(0);
        }

        if (miningPitches.size() < 10) {
            return;
        }

        // Baritone characteristics:
        // 1. Very consistent pitch (often looking down at specific angles)
        // 2. Snaps to exact same angles repeatedly
        // 3. Often uses specific angles like 45, 60, or straight down

        // Check pitch consistency
        double pitchVariance = calculateVariance(miningPitches);
        // Extremely low variance (< 0.001) is physically impossible for humans
        // This catches Baritone's perfect angle reproduction
        boolean roboticPrecision = pitchVariance < 0.001;
        // Low variance is suspicious - increased threshold to catch more bots
        // Variance under 50 with consistent mining is very suspicious
        boolean consistentPitch = pitchVariance < 50.0 && pitchVariance > 0.001;

        // Check for repeated exact angles
        int exactMatches = 0;
        for (int i = 0; i < miningPitches.size() - 1; i++) {
            for (int j = i + 1; j < miningPitches.size(); j++) {
                if (Math.abs(miningPitches.get(i) - miningPitches.get(j)) < 0.1f) {
                    exactMatches++;
                }
            }
        }
        
        // Lowered threshold - 30% exact matches is already very suspicious for mining
        boolean manyExactMatches = exactMatches > (miningPitches.size() * (miningPitches.size() - 1) / 2) * 0.3;

        // Check for common Baritone angles
        int commonAngles = 0;
        for (float p : miningPitches) {
            // Common bot angles: 0, 45, 60, 90 degrees
            if (Math.abs(p) < 2 || Math.abs(p - 45) < 2 || Math.abs(p - 60) < 2 || Math.abs(p - 90) < 2) {
                commonAngles++;
            }
        }
        
        boolean usesCommonAngles = commonAngles > miningPitches.size() * 0.7;

        // Check yaw snapping (Baritone snaps to cardinal directions)
        int cardinalSnaps = 0;
        for (float y : miningYaws) {
            float normalized = (y % 90);
            if (Math.abs(normalized) < 5 || Math.abs(normalized - 90) < 5) {
                cardinalSnaps++;
            }
        }
        
        boolean snapsToCardinal = cardinalSnaps > miningYaws.size() * 0.6;

        debug("Pitch variance: " + pitchVariance + " Exact matches: " + exactMatches + 
              " Common angles: " + commonAngles + " Cardinal snaps: " + cardinalSnaps);

        // Robotic precision is an instant flag - this is the smoking gun for Baritone
        if (roboticPrecision && exactMatches > 80) {
            debug("ROBOTIC PRECISION DETECTED - Variance: " + pitchVariance);
            increaseVl(3); // High confidence
            return;
        }

        int violations = 0;
        if (consistentPitch) violations++;
        if (manyExactMatches) violations++;
        if (usesCommonAngles) violations++;
        if (snapsToCardinal) violations++;

        // Lowered from 3 to 2 - auto-mining shows clear patterns
        if (violations >= 2) {
            increaseVl(violations - 1);
        }
    }

    private double calculateVariance(List<Float> values) {
        if (values.isEmpty()) return 0;
        
        double sum = 0;
        for (float val : values) {
            sum += val;
        }
        double mean = sum / values.size();
        
        double variance = 0;
        for (float val : values) {
            variance += Math.pow(val - mean, 2);
        }
        
        return variance / values.size();
    }
}

