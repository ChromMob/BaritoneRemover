package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CheckData(name = "RotationPrecision", identifier = "A", checkType = CheckType.ROTATION, description = "Detects robotic rotation precision patterns characteristic of bots")
public class RotationPrecisionA extends Check {
    private final List<Float> yawDeltas = new ArrayList<>();
    private final List<Float> pitchDeltas = new ArrayList<>();
    private int consistentPrecisionCount = 0;

    public RotationPrecisionA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        if (playerData.isCinematic()) {
            return;
        }
        
        if (playerData.packetDataList().size(CheckType.ROTATION) < 2) {
            return;
        }

        PacketData latest = playerData.packetDataList().getLatest(CheckType.ROTATION, CheckType.FLYING);
        PacketData previous = playerData.packetDataList().getPrevious(latest, CheckType.ROTATION, CheckType.FLYING);
        
        if (latest == null || previous == null) {
            return;
        }

        float yawDelta = Math.abs(latest.rotationData().yaw() - previous.rotationData().yaw());
        float pitchDelta = Math.abs(latest.rotationData().pitch() - previous.rotationData().pitch());

        // Ignore too small movements (likely standing still)
        if (yawDelta < 0.01f && pitchDelta < 0.01f) {
            return;
        }

        if (yawDelta > 0.01f) {
            yawDeltas.add(yawDelta);
        }
        if (pitchDelta > 0.01f) {
            pitchDeltas.add(pitchDelta);
        }

        // Keep last 40 samples for better accuracy
        if (yawDeltas.size() > 40) {
            yawDeltas.remove(0);
        }
        if (pitchDeltas.size() > 40) {
            pitchDeltas.remove(0);
        }

        // Need more data to avoid false positives
        if (yawDeltas.size() < 25) {
            return;
        }

        // Check for GCD patterns (bots often have consistent smallest rotation unit)
        float gcdYaw = findGCD(yawDeltas);
        float gcdPitch = findGCD(pitchDeltas);

        // Check decimal precision consistency
        int precisionYaw = checkDecimalConsistency(yawDeltas);
        int precisionPitch = checkDecimalConsistency(pitchDeltas);

        // Bots tend to have very consistent GCD and precision
        // Made more strict to avoid false positives - need very high divisibility
        boolean suspiciousGCD = gcdYaw > 0 && gcdYaw < 0.5f && countDivisible(yawDeltas, gcdYaw) > yawDeltas.size() * 0.9;
        // Precision check: 5+ decimal places is common for bots (including 7-8 for some clients)
        boolean suspiciousPrecision = precisionYaw >= 5 && precisionYaw <= 8;

        debug("GCD Yaw: " + gcdYaw + " Precision: " + precisionYaw + " Samples: " + yawDeltas.size() + " Divisible: " + countDivisible(yawDeltas, gcdYaw));

        // Require both conditions to be met multiple times
        if (suspiciousGCD && suspiciousPrecision) {
            consistentPrecisionCount++;
            if (consistentPrecisionCount > 5) {
                increaseVl(1);
                consistentPrecisionCount = 0;
            }
        } else {
            // Decay more aggressively
            if (consistentPrecisionCount > 0) {
                consistentPrecisionCount = Math.max(0, consistentPrecisionCount - 2);
            }
        }
    }

    // Find approximate GCD of rotation deltas
    private float findGCD(List<Float> values) {
        if (values.isEmpty()) return 0;
        
        // Round to reasonable precision to find patterns
        List<Integer> rounded = new ArrayList<>();
        for (float val : values) {
            rounded.add(Math.round(val * 10000)); // 4 decimal places
        }
        
        int gcd = rounded.get(0);
        for (int val : rounded) {
            if (val != 0) {
                gcd = gcd(gcd, val);
            }
        }
        
        return gcd / 10000.0f;
    }

    private int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private int countDivisible(List<Float> values, float gcd) {
        if (gcd < 0.0001f) return 0;
        
        int count = 0;
        for (float val : values) {
            float remainder = (val / gcd) % 1.0f;
            if (remainder < 0.05f || remainder > 0.95f) {
                count++;
            }
        }
        return count;
    }

    // Check how many decimal places are consistently used
    private int checkDecimalConsistency(List<Float> values) {
        Map<Integer, Integer> precisionCount = new HashMap<>();
        
        for (float val : values) {
            String str = Float.toString(Math.abs(val));
            int decimalPos = str.indexOf('.');
            if (decimalPos >= 0) {
                int precision = str.length() - decimalPos - 1;
                precisionCount.put(precision, precisionCount.getOrDefault(precision, 0) + 1);
            }
        }
        
        // Find most common precision
        int maxCount = 0;
        int commonPrecision = 0;
        for (Map.Entry<Integer, Integer> entry : precisionCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                commonPrecision = entry.getKey();
            }
        }
        
        // If most values have same precision, return it
        if (maxCount > values.size() * 0.7) {
            return commonPrecision;
        }
        
        return 0;
    }
}

