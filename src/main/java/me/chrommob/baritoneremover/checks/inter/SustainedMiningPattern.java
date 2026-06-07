package me.chrommob.baritoneremover.checks.inter;

public final class SustainedMiningPattern {
    private static final int MIN_ROTATION_SAMPLES = 10;
    private static final double MIN_YAW_MOVEMENT = 4.0;
    private static final double MAX_PITCH_MOVEMENT = 0.02;
    private static final double MAX_PITCH_SPREAD = 0.01;
    private static final double BARITONE_TRAVERSE_PITCH = 26.0;
    private static final double MAX_TRAVERSE_PITCH_ERROR = 0.75;

    private SustainedMiningPattern() {
    }

    public static boolean matches(int samples, double yawMovement, double pitchMovement, double pitchSpread,
            double averagePitch) {
        return samples >= MIN_ROTATION_SAMPLES
                && yawMovement >= MIN_YAW_MOVEMENT
                && pitchMovement <= MAX_PITCH_MOVEMENT
                && pitchSpread <= MAX_PITCH_SPREAD
                && Math.abs(averagePitch - BARITONE_TRAVERSE_PITCH) <= MAX_TRAVERSE_PITCH_ERROR;
    }
}
