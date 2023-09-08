package me.chrommob.baritoneremover.data.types;

import com.github.retrooper.packetevents.util.Vector3d;

import java.util.Objects;

public class PositionData {
    private final Vector3d location;
    private final double x;
    private final double y;
    private final double z;

    public PositionData(Vector3d location) {
        this.location = location;
        x = location.x;
        y = location.y;
        z = location.z;
    }

    public double distance(Vector3d other) {
        return location.distance(other);
    }

    public Vector3d location() {
        return location;
    }

    public double differenceX(PositionData positionData) {
        return Math.abs(x - positionData.x);
    }

    public double differenceY(PositionData positionData) {
        return Math.abs(y - positionData.y);
    }

    public double differenceZ(PositionData positionData) {
        return Math.abs(z - positionData.z);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PositionData that = (PositionData) obj;
        return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
                Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "PositionData[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "z=" + z + ']';
    }
}
