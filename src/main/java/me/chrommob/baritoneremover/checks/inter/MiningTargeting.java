package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.PositionData;

public final class MiningTargeting {
    private static final long MAX_ROTATION_INTERVAL_MILLIS = 200L;
    private static final double MAX_POSITION_CHANGE = 0.05;

    private MiningTargeting() {
    }

    public static CanonicalTargeting.Result evaluate(PacketDatas packets, PacketData action, long maxActionDelayMillis) {
        if (action == null || action.blockTargetData() == null || action.blockTargetData().placement()) {
            return null;
        }
        PacketData currentRotation = packets.getPrevious(action, CheckType.ROTATION, CheckType.FLYING);
        if (currentRotation == null) {
            return null;
        }
        PacketData previousRotation = packets.getPrevious(currentRotation, CheckType.ROTATION, CheckType.FLYING);
        if (previousRotation == null) {
            return null;
        }
        long actionDelay = action.timeStamp() - currentRotation.timeStamp();
        long rotationInterval = currentRotation.timeStamp() - previousRotation.timeStamp();
        if (actionDelay < 0L || actionDelay > maxActionDelayMillis
                || rotationInterval < 0L || rotationInterval > MAX_ROTATION_INTERVAL_MILLIS) {
            return null;
        }

        PositionData position = positionAt(packets, currentRotation);
        PositionData previousPosition = positionAt(packets, previousRotation);
        PacketData latestPositionPacket = packets.getPrevious(action, CheckType.POSITION, CheckType.FLYING);
        if (position == null || previousPosition == null || latestPositionPacket == null
                || position.distance(previousPosition.location()) > MAX_POSITION_CHANGE
                || position.distance(latestPositionPacket.positionData().location()) > MAX_POSITION_CHANGE) {
            return null;
        }
        return CanonicalTargeting.evaluate(position, currentRotation.rotationData(), previousRotation.rotationData(),
                action.blockTargetData());
    }

    private static PositionData positionAt(PacketDatas packets, PacketData rotation) {
        if (rotation.positionData() != null) {
            return rotation.positionData();
        }
        PacketData position = packets.getPrevious(rotation, CheckType.POSITION, CheckType.FLYING);
        return position == null ? null : position.positionData();
    }
}
