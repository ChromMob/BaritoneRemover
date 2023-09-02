package me.chrommob.baritoneremover.checks.impl;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.PlayerData;

@CheckData(name = "AutoWalk", identifier = "A", description = "Checks if the player is walking while changing their yaw but not their pitch", checkType = CheckType.FLYING)
public class AutoWalk extends Check {

    public AutoWalk(PlayerData playerData) {
        super(playerData);
    }

    private double distanceMoved = 0;
    private int ticks = 0;
    @Override
    public void run(CheckType updateType) {
        PacketDatas packetDataList = playerData.packetDataList();
        if (packetDataList.size(CheckType.FLYING) < 2) {
            return;
        }
        PacketData latest = packetDataList.getLatest(CheckType.FLYING);
        if (latest == null) {
            return;
        }
        if (Math.abs(latest.rotationData().pitch()) == 90) {
            return;
        }
        PacketData previous = packetDataList.getPrevious(latest, CheckType.FLYING);
        float differencePitch = latest.differencePitch(previous);
        if (differencePitch > 0) {
            return;
        }
        double distance = latest.distance(previous);
        distanceMoved += distance;
        ticks++;
        if (distanceMoved < 5) {
            return;
        }
        if (ticks < (distanceMoved - distanceMoved / 7)) {
            return;
        }
        increaseVl(1);
        distanceMoved = 0;
        ticks = 0;
    }
}
