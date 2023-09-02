package me.chrommob.baritoneremover.data.types;

import me.chrommob.baritoneremover.checks.inter.CheckType;

import java.util.Objects;

public final class PacketData {
    private final int index;
    private final long timeStamp;
    private final PositionData positionData;
    private final RotationData rotationData;
    private final boolean mining;
    private final boolean finishedMining;

    public PacketData(int index, long timeStamp, PositionData positionData, RotationData rotationData, boolean mining, boolean finishedMining) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.positionData = positionData;
        this.rotationData = rotationData;
        this.mining = mining;
        this.finishedMining = finishedMining;
    }

    public int index() {
        return index;
    }

    public long timeStamp() {
        return timeStamp;
    }

    public PositionData positionData() {
        return positionData;
    }

    public RotationData rotationData() {
        return rotationData;
    }

    public boolean mining() {
        return mining;
    }

    public boolean finishedMining() {
        return finishedMining;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PacketData that = (PacketData) obj;
        return this.index == that.index &&
                this.timeStamp == that.timeStamp &&
                Objects.equals(this.positionData, that.positionData) &&
                Objects.equals(this.rotationData, that.rotationData) &&
                this.mining == that.mining &&
                this.finishedMining == that.finishedMining;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, timeStamp, positionData, rotationData, mining, finishedMining);
    }

    @Override
    public String toString() {
        return "PacketData[" +
                "index=" + index + ", " +
                "timeStamp=" + timeStamp + ", " +
                "positionData=" + positionData + ", " +
                "rotationData=" + rotationData + ", " +
                "mining=" + mining + ", " +
                "finishedMining=" + finishedMining + ']';
    }

    public double distance(PacketData other) {
        return positionData.distance(other.positionData().location());
    }

    public float distanceRotation(PacketData other) {
        return rotationData.distance(other.rotationData());
    }

    public float differencePitch(PacketData other) {
        return rotationData.differencePitch(other.rotationData());
    }

    public float differenceYaw(PacketData other) {
        return rotationData.differenceYaw(other.rotationData());
    }

    public CheckType checkType() {
        if (positionData == null && rotationData != null) {
            return CheckType.ROTATION;
        } else if (positionData != null && rotationData == null) {
            return CheckType.POSITION;
        } else if (positionData != null) {
            return CheckType.FLYING;
        } else if (mining) {
            return CheckType.MINING;
        } else if (finishedMining) {
            return CheckType.MINED;
        } else {
            return CheckType.NONE;
        }
    }
}
