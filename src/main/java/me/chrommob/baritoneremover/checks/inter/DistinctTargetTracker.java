package me.chrommob.baritoneremover.checks.inter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DistinctTargetTracker {
    private final long windowMillis;
    private final Map<String, Long> targets = new LinkedHashMap<>();

    public DistinctTargetTracker(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    public synchronized int record(String target, long now) {
        Iterator<Map.Entry<String, Long>> iterator = targets.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().getValue() > windowMillis) {
                iterator.remove();
            }
        }
        if (targets.containsKey(target)) {
            return 0;
        }
        targets.put(target, now);
        return targets.size();
    }
}
