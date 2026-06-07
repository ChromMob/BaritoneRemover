package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.data.types.BlockTargetData;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;

public final class CanonicalTargeting {
    private static final double MAX_LOCK_ERROR = 0.8;
    private static final double MIN_PREVIOUS_ERROR = 2.25;
    private static final double MIN_DISTANCE = 1.0;
    private static final double MAX_DISTANCE = 5.25;
    private static final float MIN_TURN = 2.0f;
    private static final double MAX_MINING_PITCH_ERROR = 0.35;
    private static final double MAX_MINING_YAW_ERROR = 1.35;
    private static final double MIN_MINING_PREVIOUS_ERROR = 3.0;
    private static final float MIN_MINING_TURN = 3.0f;

    private CanonicalTargeting() {
    }

    public static Result evaluate(PositionData position, RotationData current, RotationData previous,
            BlockTargetData target) {
        Result best = null;
        double eyeHeight = target.eyeHeight();
        if (target.includeBlockCenter()) {
            best = closer(best, evaluatePoint(position, eyeHeight, current, previous, target.x() + 0.5,
                    target.y() + 0.5, target.z() + 0.5));
        }
        if (target.hasValidFace()) {
            if (target.placement()) {
                // Baritone's pathing placer deliberately aims at this lower-quarter point.
                best = closer(best, evaluatePoint(position, eyeHeight, current, previous,
                        target.x() + 0.5 + target.faceX() * 0.5, target.y() + 0.25 + target.faceY() * 0.5,
                        target.z() + 0.5 + target.faceZ() * 0.5));
            } else {
                best = closer(best, evaluatePoint(position, eyeHeight, current, previous,
                        target.x() + 0.5 + target.faceX() * 0.5, target.y() + 0.5 + target.faceY() * 0.5,
                        target.z() + 0.5 + target.faceZ() * 0.5));
            }
        }
        return best;
    }

    private static Result closer(Result currentBest, Result candidate) {
        if (currentBest == null || candidate.currentError() < currentBest.currentError()) {
            return candidate;
        }
        return currentBest;
    }

    private static Result evaluatePoint(PositionData position, double eyeHeight, RotationData current,
            RotationData previous, double targetX, double targetY, double targetZ) {
        double eyeX = position.x();
        double eyeY = position.y() + eyeHeight;
        double eyeZ = position.z();
        double deltaX = eyeX - targetX;
        double deltaY = eyeY - targetY;
        double deltaZ = eyeZ - targetZ;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float expectedYaw = (float) Math.toDegrees(Math.atan2(deltaX, -deltaZ));
        float expectedPitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        RotationData expected = new RotationData(expectedPitch, expectedYaw);
        double currentYawError = current.differenceYaw(expected);
        double currentPitchError = current.differencePitch(expected);
        double currentError = angularError(current, expected);
        double previousError = angularError(previous, expected);
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        return new Result(currentError, previousError, currentYawError, currentPitchError, distance,
                current.distance(previous));
    }

    private static double angularError(RotationData actual, RotationData expected) {
        return Math.hypot(actual.differenceYaw(expected), actual.differencePitch(expected));
    }

    public static final class Result {
        private final double currentError;
        private final double previousError;
        private final double currentYawError;
        private final double currentPitchError;
        private final double distance;
        private final float turn;

        private Result(double currentError, double previousError, double currentYawError, double currentPitchError,
                double distance, float turn) {
            this.currentError = currentError;
            this.previousError = previousError;
            this.currentYawError = currentYawError;
            this.currentPitchError = currentPitchError;
            this.distance = distance;
            this.turn = turn;
        }

        public boolean isCanonicalSnap() {
            return currentError <= MAX_LOCK_ERROR
                    && previousError >= MIN_PREVIOUS_ERROR
                    && distance >= MIN_DISTANCE
                    && distance <= MAX_DISTANCE
                    && turn >= MIN_TURN;
        }

        public boolean isBaritoneMiningApproach() {
            return isBaritoneMiningLock()
                    && previousError >= MIN_MINING_PREVIOUS_ERROR
                    && turn >= MIN_MINING_TURN;
        }

        public boolean isBaritoneMiningLock() {
            return currentYawError <= MAX_MINING_YAW_ERROR
                    && currentPitchError <= MAX_MINING_PITCH_ERROR
                    && distance >= MIN_DISTANCE
                    && distance <= MAX_DISTANCE;
        }

        public double currentError() {
            return currentError;
        }

        public double previousError() {
            return previousError;
        }

        public double currentYawError() {
            return currentYawError;
        }

        public double currentPitchError() {
            return currentPitchError;
        }

        public double distance() {
            return distance;
        }

        public float turn() {
            return turn;
        }
    }
}
