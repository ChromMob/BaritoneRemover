package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

@CheckData(name = "TimeBetween", identifier = "B", description = "Checks the time it took to place a block after placing the previous block", checkType = CheckType.PLACE)
public class TimeBetweenB extends Check {
    public TimeBetweenB(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        PacketDatas packetDatas = playerData.packetDataList();
        if (packetDatas.size(checkType()) < 2) {
            return;
        }
        PacketData latest = packetDatas.getLatest(checkType());
        PacketData previous = packetDatas.getPrevious(latest, checkType());
        PacketData latestRotation = packetDatas.getLatest(CheckType.ROTATION);
        PacketData latestFlying = packetDatas.getLatest(CheckType.FLYING);
        if (latestRotation == null && latestFlying == null) {
            return;
        }
        PacketData latestRotationInfo;
        if (latestRotation == null) {
            latestRotationInfo = latestFlying;
        } else if (latestFlying == null) {
            latestRotationInfo = latestRotation;
        } else {
            latestRotationInfo = latestRotation.timeStamp() > latestFlying.timeStamp() ? latestRotation : latestFlying;
        }
        PacketData previousRotation = packetDatas.getPrevious(previous, CheckType.ROTATION);
        PacketData previousFlying = packetDatas.getPrevious(previous, CheckType.FLYING);
        if (previousRotation == null && previousFlying == null) {
            return;
        }
        PacketData previousRotationInfo;
        if (previousRotation == null) {
            previousRotationInfo = previousFlying;
        } else if (previousFlying == null) {
            previousRotationInfo = previousRotation;
        } else {
            previousRotationInfo = previousRotation.timeStamp() > previousFlying.timeStamp() ? previousRotation : previousFlying;
        }
        float distance = latestRotationInfo.rotationData().distance(previousRotationInfo.rotationData());
        debug("distanceB: " + distance);
        if (distance < 3 || distance > 200) {
            return;
        }
        long time = latest.timeStamp() - previous.timeStamp();
        if (time <= 0) {
            debug("timeB is lower or equal to 0 this shouldn't happen (" + time + ")");
            return;
        }
        float timeToRotate = (float) time / distance;
        debug("timeToRotateB: " + timeToRotate);
        if (timeToRotate > 2) {
            return;
        }
        increaseVl(Math.round((1/timeToRotate)));
    }
}
