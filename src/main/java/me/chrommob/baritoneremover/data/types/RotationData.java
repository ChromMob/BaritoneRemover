package me.chrommob.baritoneremover.data.types;

import java.util.Objects;

public final class RotationData {
    private final float pitch;
    private final float yaw;

    public RotationData(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float pitch() {
        return pitch;
    }

    public float yaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        RotationData that = (RotationData) obj;
        return Float.floatToIntBits(this.pitch) == Float.floatToIntBits(that.pitch) &&
                Float.floatToIntBits(this.yaw) == Float.floatToIntBits(that.yaw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pitch, yaw);
    }

    @Override
    public String toString() {
        return "RotationData[" +
                "pitch=" + pitch + ", " +
                "yaw=" + yaw + ']';
    }

    public float distance(RotationData other) {
        return Math.abs(pitch - other.pitch()) + Math.abs(yaw - other.yaw());
    }

    public float differencePitch(RotationData other) {
        return Math.abs(pitch - other.pitch());
    }

    public float differenceYaw(RotationData other) {
        return Math.abs(yaw - other.yaw());
    }
}
