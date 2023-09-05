package me.chrommob.baritoneremover.data;

import com.github.retrooper.packetevents.util.Vector3d;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class PlayerData {
    private final String name;
    private final boolean isBedrock;
    private ItemStack itemInHand;
    private boolean debug = false;
    private PacketDatas packetDataList = new PacketDatas();
    public PlayerData(String name, Checks checks) {
        this.name = name;
        checks.getChecks().forEach(check -> {
            try {
                this.checks.add(check.getConstructor(PlayerData.class).newInstance(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        isBedrock = ConfigManager.getInstance().floodgateApi() != null && ConfigManager.getInstance().floodgateApi().isFloodgatePlayer(Bukkit.getPlayer(name).getUniqueId());
    }

    private final Set<Check> checks = new HashSet<>();

    public void updatePosition(Vector3d location) {
        packetDataList.add(CheckType.POSITION, new PositionData(location), null, false, false, false);
        runChecks(CheckType.POSITION);
        runChecks(CheckType.ANY);
    }

    public void updateRotation(float pitch, float yaw) {
        packetDataList.add(CheckType.ROTATION, null, new RotationData(pitch, yaw), false, false, false);
        runChecks(CheckType.ROTATION);
        runChecks(CheckType.ANY);
    }

    public void updateBoth(Vector3d position, float pitch, float yaw) {
        packetDataList.add(CheckType.FLYING, new PositionData(position), new RotationData(pitch, yaw), false, false, false);
        runChecks(CheckType.FLYING);
        runChecks(CheckType.ANY);
    }

    public void startMining() {
        packetDataList.add(CheckType.MINING, null, null, true, false, false);
        runChecks(CheckType.MINING);
        runChecks(CheckType.ANY);
    }

    public void finishMining() {
        packetDataList.add(CheckType.MINED, null, null, false, true, false);
        runChecks(CheckType.MINED);
        runChecks(CheckType.ANY);
    }

    public void blockPlace() {
        packetDataList.add(CheckType.PLACE, null, null, false, false, true);
        runChecks(CheckType.PLACE);
        runChecks(CheckType.ANY);
    }

    public void setItemInHand(ItemStack itemInHand) {
        this.itemInHand = itemInHand;
    }

    public void debug() {
        debug = !debug;
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
        if (packetDataList.size() == 0) {
            return;
        }
        if (packetDataList.size() % 100 == 0) {
            checks.forEach(check -> {
                if (check.checkType() == CheckType.AGGREGATE) {
                    check.run(updateType);
                }
            });
        }
        if (packetDataList.size() > 1000) {
            packetDataList = new PacketDatas();
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isBedrock() {
        return isBedrock;
    }

    public boolean hasBlockInHand() {
        return itemInHand != null && itemInHand.getType().isBlock();
    }

    public PacketDatas packetDataList() {
        return packetDataList;
    }

    public Set<Check> checks() {
        return checks;
    }
}