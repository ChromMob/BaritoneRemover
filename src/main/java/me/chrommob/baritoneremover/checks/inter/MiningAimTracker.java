package me.chrommob.baritoneremover.checks.inter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalDouble;

public final class MiningAimTracker {
    private static final double MIN_PITCH_SPREAD = 8.0;
    private final int requiredMatches;
    private final int maxTargets;
    private final long windowMillis;
    private final Map<String, Sample> targets = new LinkedHashMap<>();

    public MiningAimTracker(int requiredMatches, int maxTargets, long windowMillis) {
        this.requiredMatches = requiredMatches;
        this.maxTargets = maxTargets;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean record(String target, OptionalDouble expectedPitch, long now) {
        Iterator<Map.Entry<String, Sample>> iterator = targets.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue().time > windowMillis) {
                iterator.remove();
            }
        }
        if (targets.containsKey(target)) {
            return false;
        }
        targets.put(target, new Sample(expectedPitch, now));
        if (targets.size() > maxTargets) {
            targets.remove(targets.keySet().iterator().next());
        }
        if (targets.size() < requiredMatches) {
            return false;
        }
        double minimumPitch = Double.POSITIVE_INFINITY;
        double maximumPitch = Double.NEGATIVE_INFINITY;
        int matches = 0;
        for (Sample sample : targets.values()) {
            if (!sample.pitch.isPresent()) {
                continue;
            }
            matches++;
            minimumPitch = Math.min(minimumPitch, sample.pitch.getAsDouble());
            maximumPitch = Math.max(maximumPitch, sample.pitch.getAsDouble());
        }
        if (matches < requiredMatches || maximumPitch - minimumPitch < MIN_PITCH_SPREAD) {
            return false;
        }
        targets.clear();
        return true;
    }

    private static final class Sample {
        private final OptionalDouble pitch;
        private final long time;

        private Sample(OptionalDouble pitch, long time) {
            this.pitch = pitch;
            this.time = time;
        }
    }
}
