package me.chrommob.baritoneremover.checks.inter;

public class GraphResult {
    public GraphResult(int positives, int negatives) {
        this.positives = positives;
        this.negatives = negatives;
    }

    public final int positives, negatives;
}
