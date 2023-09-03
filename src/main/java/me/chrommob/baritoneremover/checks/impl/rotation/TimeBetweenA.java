package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

@CheckData(name = "TimeBetween", identifier = "A", description = "Checks the time it took to start mining a block after mining the previous block", checkType = CheckType.MINING)
public class TimeBetweenA extends Check {
    public TimeBetweenA(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        PacketDatas packetDatas = playerData.packetDataList();
        //Shouldn't happen but just in case
        if (packetDatas.size(CheckType.MINING) == 0) {
            return;
        }
        //Shouldn't happen but just in case
        PacketData latest = packetDatas.getLatest(CheckType.MINING);
        if (latest == null) {
            return;
        }
        //Check if the player previously mined a block we don't want to flag them if they haven't
        PacketData previous = packetDatas.getPrevious(latest, CheckType.MINED);
        if (previous == null) {
            return;
        }
        //We need to find latest rotation data of the player before they started mining the block
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
        //We need to find the previous rotation data of the player before they finished mining the previous block
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
        long time = latest.timeStamp() - previous.timeStamp();
        float timeToRotate = (float) time / distance;
        //If the player rotated that fast, they are probably using a bot
        debug("timeToRotateA: " + timeToRotate);
        if (timeToRotate > 2) {
            return;
        }
        increaseVl(Math.round((1/timeToRotate)));
    }
}
