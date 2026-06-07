package me.chrommob.baritoneremover.data.types;

public final class BlockTargetData {
    private final int x;
    private final int y;
    private final int z;
    private final int faceX;
    private final int faceY;
    private final int faceZ;
    private final boolean includeBlockCenter;
    private final boolean placement;
    private final double eyeHeight;

    private BlockTargetData(int x, int y, int z, int faceX, int faceY, int faceZ, boolean includeBlockCenter,
            boolean placement, double eyeHeight) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.faceX = faceX;
        this.faceY = faceY;
        this.faceZ = faceZ;
        this.includeBlockCenter = includeBlockCenter;
        this.placement = placement;
        this.eyeHeight = eyeHeight;
    }

    public static BlockTargetData mining(int x, int y, int z, int faceX, int faceY, int faceZ, double eyeHeight) {
        return new BlockTargetData(x, y, z, faceX, faceY, faceZ, true, false, eyeHeight);
    }

    public static BlockTargetData placement(int againstX, int againstY, int againstZ, int placedX, int placedY,
            int placedZ, double eyeHeight) {
        return new BlockTargetData(againstX, againstY, againstZ, placedX - againstX, placedY - againstY,
                placedZ - againstZ, false, true, eyeHeight);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int faceX() {
        return faceX;
    }

    public int faceY() {
        return faceY;
    }

    public int faceZ() {
        return faceZ;
    }

    public boolean includeBlockCenter() {
        return includeBlockCenter;
    }

    public boolean placement() {
        return placement;
    }

    public double eyeHeight() {
        return eyeHeight;
    }

    public boolean hasValidFace() {
        return Math.abs(faceX) + Math.abs(faceY) + Math.abs(faceZ) == 1;
    }

    public String key() {
        return x + ":" + y + ":" + z;
    }
}
