package me.chrommob.baritoneremover.data;

import com.github.retrooper.packetevents.util.Vector3d;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.print.attribute.standard.Severity;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerData {
    private final String name;
    private final boolean isBedrock;
    private boolean debug = false;
    private boolean isCinematic = false;
    private PacketDatas packetDataList = new PacketDatas();
    private long lastTPSCheck = System.currentTimeMillis();
    public PlayerData(String name, Checks checks) {
        Bukkit.getScheduler().runTaskTimer(BaritoneRemover.getPlugin(BaritoneRemover.class), () -> {
            long currentTime = System.currentTimeMillis();
            long difference = currentTime - lastTPSCheck;
            double TPS = 20.0 / (difference / 1000.0);
            this.TPS.set(TPS < ConfigManager.getInstance().minTps());
            lastTPSCheck = currentTime;
        }, 20L, 20L);
        this.name = name;
        checks.getChecks().forEach(check -> {
            try {
                this.checks.add(check.getConstructor(PlayerData.class).newInstance(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        isBedrock = ConfigManager.getInstance().floodgateApi() != null
                && ConfigManager.getInstance().floodgateApi().isFloodgatePlayer(Bukkit.getPlayer(name).getUniqueId());
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
        packetDataList.add(CheckType.FLYING, new PositionData(position), new RotationData(pitch, yaw), false, false,
                false);
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
        Bukkit.getScheduler().runTaskAsynchronously(ConfigManager.getInstance().plugin(), () -> {
            runChecks(CheckType.PLACE);
            runChecks(CheckType.ANY);
        });
    }

    public void debug() {
        debug = !debug;
    }

    public String name() {
        return name;
    }

    private final AtomicBoolean TPS = new AtomicBoolean(false);
    public void runChecks(CheckType updateType) {
        if (updateType == CheckType.NONE) {
            return;
        }
        if (TPS.get()) return;
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.hasPermission("br.bypass")) {
            return;
        }
        checks.forEach(check -> {
            if (check.checkType() != updateType) {
                return;
            }
            check.run();
        });
        if (packetDataList.size() == 0) {
            return;
        }
        if (packetDataList.size() % 100 == 0) {
            checks.forEach(check -> {
                if (check.checkType() == CheckType.AGGREGATE) {
                    check.run();
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

    public PacketDatas packetDataList() {
        return packetDataList;
    }

    public Set<Check> checks() {
        return checks;
    }

    public void setCinematic(boolean cinematic) {
        isCinematic = cinematic;
    }

    public boolean isCinematic() {
        return isCinematic;
    }
}