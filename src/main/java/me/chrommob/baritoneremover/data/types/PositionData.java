package me.chrommob.baritoneremover.data.types;

import com.github.retrooper.packetevents.util.Vector3d;

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

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }
}
