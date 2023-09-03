package me.chrommob.baritoneremover.checks.impl.autowalk;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;

@CheckData(name = "AutoWalk", identifier = "A", description = "Checks if the player is walking while changing their yaw but not their pitch", checkType = CheckType.FLYING)
public class AutoWalkA extends Check {

    public AutoWalkA(PlayerData playerData) {
        super(playerData);
    }

    private double distanceMoved = 0;
    private int ticks = 0;
    @Override
    public void run(CheckType updateType) {
        //Bedrock players can't move their pitch while in a boat
        if (playerData.isBedrock() && Bukkit.getPlayer(playerData.name()) != null && Bukkit.getPlayer(playerData.name()).getVehicle() instanceof Boat) {
            return;
        }
        PacketDatas packetDataList = playerData.packetDataList();
        //We need at least 2 flying packets to check this
        if (packetDataList.size(CheckType.FLYING) < 2) {
            return;
        }
        PacketData latest = packetDataList.getLatest(CheckType.FLYING);
        //Shouldn't happen but just in case
        if (latest == null) {
            return;
        }
        //If the player is looking straight up or down, they can move without changing their pitch so we don't want to flag them
        if (Math.abs(latest.rotationData().pitch()) == 90) {
            return;
        }
        PacketData previous = packetDataList.getPrevious(latest, CheckType.FLYING);
        float differencePitch = latest.differencePitch(previous);
        //If there is no difference there is no way to tell if they are auto walking
        if (differencePitch > 0) {
            if (ticks > 0)
                ticks--;
            return;
        }
        double distance = latest.distance(previous);
        distanceMoved += distance;
        ticks++;
        //If the player has moved less than 5 blocks, we don't want to flag them
        if (distanceMoved < 20) {
            return;
        }
        if (ticks/distanceMoved < 3) {
            debug("Ticks: " + ticks + " Distance: " + distanceMoved + " Ticks/Distance: " + ticks/distanceMoved);
            distanceMoved = 0;
            ticks = 0;
            return;
        }
        increaseVl(Math.round(Math.round((ticks/distanceMoved)/3)));
        distanceMoved = 0;
        ticks = 0;
    }
}
