package me.chrommob.baritoneremover.checks.inter;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<Integer> computePattern(List<Integer> indexes) {
        List<Integer> pattern = new ArrayList<>();
        int diff = indexes.get(1) - indexes.get(0);

        for (int i = 1; i < indexes.size(); i++) {
            pattern.add(indexes.get(i) - indexes.get(i - 1));
        }

        pattern.add(0, diff); // Add the initial difference as the first element
        return pattern;
    }
}
