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

    public static GraphResult getGraph(List<Double> values) {
        double largest = 0;

        for (double value : values) {
            if (value > largest)
                largest = value;
        }

        int GRAPH_HEIGHT = 2;
        int positives = 0, negatives = 0;

        for (int i = GRAPH_HEIGHT - 1; i > 0; i -= 1) {

            for (double index : values) {
                double value = GRAPH_HEIGHT * index / largest;

                if (value > i && value < i + 1) {
                    ++positives;
                } else {
                    ++negatives;
                }
            }
        }

        return new GraphResult(positives, negatives);
    }

}

