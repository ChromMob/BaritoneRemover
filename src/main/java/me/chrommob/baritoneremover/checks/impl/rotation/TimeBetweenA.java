package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "TimeBetween", identifier = "A", description = "Checks the time it took to start mining a block after mining the previous block", checkType = CheckType.MINING)
public class TimeBetweenA extends Check {
    public TimeBetweenA(PlayerData playerData) {
        super(playerData);
    }

    private List<Float> lastRotates = new ArrayList<>();

    @Override
    public void run() {
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
        if (distance == 0) {
            return;
        }
        long time = latest.timeStamp() - previous.timeStamp();
        if (time <= 0) {
            debug("timeA is lower or equal to 0 this shouldn't happen (" + time + ")");
            return;
        }
        float timeToRotate = (float) time / distance;
        if (lastRotates.size() >= 10) {
            lastRotates.remove(0);
        }
        lastRotates.add(timeToRotate);
        if (lastRotates.size() < 2) {
            return;
        }
        double sum = 0;
        for (float number : lastRotates) {
            sum += number;
        }
        double mean = sum / lastRotates.size();

        double squaredDifferencesSum = 0;
        for (float number : lastRotates) {
            double difference = number - mean;
            squaredDifferencesSum += difference * difference;
        }

        double meanOfSquaredDifferences = squaredDifferencesSum / lastRotates.size();

        double standardDeviation = Math.sqrt(meanOfSquaredDifferences);
        if (standardDeviation < 10000)
            return;
        debug("timeToRotateA: " + timeToRotate + " standardDeviation: " + standardDeviation);
        if (timeToRotate > 2) {
            return;
        }
        increaseVl(Math.round((1/timeToRotate)));
    }
}
