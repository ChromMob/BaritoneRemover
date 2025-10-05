package me.chrommob.baritoneremover.checks.impl.movement;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.PositionData;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "Pathfinding", identifier = "A", checkType = CheckType.AGGREGATE, description = "Detects A* pathfinding patterns characteristic of Baritone")
public class PathfindingA extends Check {
    
    public PathfindingA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        List<PacketData> packets = playerData.packetDataList().getAllType(CheckType.FLYING, CheckType.POSITION);
        
        if (packets.size() < 20) {
            return;
        }

        // Analyze movement vectors for pathfinding signatures
        List<MovementVector> vectors = new ArrayList<>();
        for (int i = 0; i < packets.size() - 1; i++) {
            PositionData current = packets.get(i).positionData();
            PositionData next = packets.get(i + 1).positionData();
            
            double dx = next.x() - current.x();
            double dy = next.y() - current.y();
            double dz = next.z() - current.z();
            
            // Ignore stationary packets
            if (Math.abs(dx) < 0.001 && Math.abs(dy) < 0.001 && Math.abs(dz) < 0.001) {
                continue;
            }
            
            vectors.add(new MovementVector(dx, dy, dz));
        }

        if (vectors.size() < 15) {
            return;
        }

        int suspiciousPatterns = 0;

        // Check for perfect cardinal/diagonal movements (pathfinding grid)
        int perfectAngles = 0;
        for (MovementVector vec : vectors) {
            if (vec.isPerfectCardinal() || vec.isPerfectDiagonal()) {
                perfectAngles++;
            }
        }
        
        if (perfectAngles > vectors.size() * 0.6) {
            suspiciousPatterns++;
            debug("Perfect angles: " + perfectAngles + "/" + vectors.size());
        }

        // Check for sudden direction changes (pathfinding nodes)
        int suddenChanges = 0;
        for (int i = 0; i < vectors.size() - 1; i++) {
            double angle = vectors.get(i).angleTo(vectors.get(i + 1));
            // Angle close to 90 degrees (pathfinding turns)
            if (Math.abs(angle - 90) < 15 || Math.abs(angle - 270) < 15) {
                suddenChanges++;
            }
        }
        
        if (suddenChanges > vectors.size() * 0.15) {
            suspiciousPatterns++;
            debug("Sudden changes: " + suddenChanges + "/" + vectors.size());
        }

        // Check for consistent speed (bots maintain exact speed)
        List<Double> speeds = new ArrayList<>();
        for (MovementVector vec : vectors) {
            speeds.add(vec.magnitude());
        }
        
        double speedVariance = calculateVariance(speeds);
        if (speedVariance < 0.001 && speeds.size() > 10) {
            suspiciousPatterns++;
            debug("Low speed variance: " + speedVariance);
        }

        // Check for grid-aligned positions (Baritone uses block-based pathfinding)
        int gridAligned = 0;
        for (PacketData packet : packets) {
            PositionData pos = packet.positionData();
            // Check if position is close to block centers or edges
            double xFrac = Math.abs((pos.x() % 1.0) - 0.5);
            double zFrac = Math.abs((pos.z() % 1.0) - 0.5);
            
            if ((xFrac < 0.1 || xFrac > 0.4) && (zFrac < 0.1 || zFrac > 0.4)) {
                gridAligned++;
            }
        }
        
        if (gridAligned > packets.size() * 0.7) {
            suspiciousPatterns++;
            debug("Grid aligned positions: " + gridAligned + "/" + packets.size());
        }

        debug("Suspicious patterns: " + suspiciousPatterns);
        
        // 2+ patterns is enough - Baritone has very distinctive signatures
        if (suspiciousPatterns >= 2) {
            increaseVl(suspiciousPatterns);
        }
    }

    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) return 0;
        
        double sum = 0;
        for (double val : values) {
            sum += val;
        }
        double mean = sum / values.size();
        
        double variance = 0;
        for (double val : values) {
            variance += Math.pow(val - mean, 2);
        }
        
        return variance / values.size();
    }

    private static class MovementVector {
        final double dx, dy, dz;
        
        MovementVector(double dx, double dy, double dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }
        
        double magnitude() {
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        boolean isPerfectCardinal() {
            // Moving along one axis only
            int nonZero = 0;
            if (Math.abs(dx) > 0.01) nonZero++;
            if (Math.abs(dy) > 0.01) nonZero++;
            if (Math.abs(dz) > 0.01) nonZero++;
            return nonZero == 1;
        }
        
        boolean isPerfectDiagonal() {
            // Moving along exactly two axes with similar magnitude
            int nonZero = 0;
            List<Double> values = new ArrayList<>();
            
            if (Math.abs(dx) > 0.01) {
                nonZero++;
                values.add(Math.abs(dx));
            }
            if (Math.abs(dy) > 0.01) {
                nonZero++;
                values.add(Math.abs(dy));
            }
            if (Math.abs(dz) > 0.01) {
                nonZero++;
                values.add(Math.abs(dz));
            }
            
            if (nonZero == 2) {
                double ratio = values.get(0) / values.get(1);
                return ratio > 0.8 && ratio < 1.2; // Similar magnitude
            }
            
            return false;
        }
        
        double angleTo(MovementVector other) {
            // Calculate angle between two vectors in degrees
            double dot = this.dx * other.dx + this.dz * other.dz; // Ignore Y for horizontal angle
            double mag1 = Math.sqrt(this.dx * this.dx + this.dz * this.dz);
            double mag2 = Math.sqrt(other.dx * other.dx + other.dz * other.dz);
            
            if (mag1 < 0.001 || mag2 < 0.001) return 0;
            
            double cosAngle = dot / (mag1 * mag2);
            cosAngle = Math.max(-1, Math.min(1, cosAngle)); // Clamp to [-1, 1]
            
            return Math.toDegrees(Math.acos(cosAngle));
        }
    }
}

