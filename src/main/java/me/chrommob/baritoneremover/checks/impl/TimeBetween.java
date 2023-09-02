package me.chrommob.baritoneremover.checks.impl;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

@CheckData(name = "TimeBetween", identifier = "A", description = "Checks the time it took to start mining a block after mining the previous block", checkType = CheckType.MINING)
public class TimeBetween extends Check {
    public TimeBetween(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        PacketDatas packetDatas = playerData.packetDataList();
        if (packetDatas.size(CheckType.MINING) == 0) {
            return;
        }
        PacketData latest = packetDatas.getLatest(CheckType.MINING);
        if (latest == null) {
            return;
        }
        PacketData previous = packetDatas.getPrevious(latest, CheckType.MINED);
        if (previous == null) {
            return;
        }
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
        PacketData previousRotation = packetDatas.getPrevious(latestRotationInfo, CheckType.ROTATION);
        PacketData previousFlying = packetDatas.getPrevious(latestRotationInfo, CheckType.FLYING);
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
        if (timeToRotate > 5) {
            return;
        }
        increaseVl(1);
    }
}
