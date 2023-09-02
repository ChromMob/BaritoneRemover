package me.chrommob.baritoneremover.data;

import com.github.retrooper.packetevents.util.Vector3d;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {
    private final String name;
    private final PacketDatas packetDataList = new PacketDatas();
    public PlayerData(String name, Checks checks) {
        this.name = name;
        checks.getChecks().forEach(check -> {
            try {
                this.checks.add(check.getConstructor(PlayerData.class).newInstance(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private final Set<Check> checks = new HashSet<>();

    public void updatePosition(Vector3d location) {
        packetDataList.add(new PositionData(location), null, false, false);
        runChecks(CheckType.POSITION);
    }

    public void updateRotation(float pitch, float yaw) {
        packetDataList.add(null, new RotationData(pitch, yaw), false, false);
        runChecks(CheckType.ROTATION);
    }

    public void updateBoth(Vector3d position, float pitch, float yaw) {
        packetDataList.add(new PositionData(position), new RotationData(pitch, yaw), false, false);
        runChecks(CheckType.FLYING);
    }

    public void startMining() {
        packetDataList.add(null, null, true, false);
        runChecks(CheckType.MINING);
    }

    public void finishMining() {
        packetDataList.add(null, null, false, true);
        runChecks(CheckType.MINED);
    }

    public String name() {
        return name;
    }

    public void runChecks(CheckType updateType) {
        if (updateType == CheckType.NONE) {
            return;
        }
        checks.forEach(check -> {
            if (check.checkType() != updateType) {
                return;
            }
            check.run(updateType);
        });
    }

    public PacketDatas packetDataList() {
        return packetDataList;
    }
}