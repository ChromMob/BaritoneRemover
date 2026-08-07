package me.chrommob.baritoneremover.data;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerData {
    private final String name;
    private final boolean isBedrock;
    private final AtomicBoolean debugConsole = new AtomicBoolean(false);
    private final Set<String> debugPlayers = ConcurrentHashMap.newKeySet();
    private boolean isCinematic = false;
    private PacketDatas packetDataList = new PacketDatas();
    private long lastTPSCheck = System.currentTimeMillis();
    public PlayerData(String name, Checks checks) {
        BaritoneRemover.scheduler().runTimer(() -> {
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

    public void startMining(BlockTargetData blockTargetData) {
        packetDataList.add(CheckType.MINING, null, null, true, false, false, blockTargetData);
        if (isDebug()) {
            sendDebug(Component.text("Mining START " + blockTargetData.key() + " face=" + blockTargetData.faceX()
                    + "," + blockTargetData.faceY() + "," + blockTargetData.faceZ()));
        }
        runChecks(CheckType.MINING);
        runChecks(CheckType.ANY);
    }

    public void finishMining() {
        packetDataList.add(CheckType.MINED, null, null, false, true, false);
        if (isDebug()) {
            sendDebug(Component.text("Mining FINISH"));
        }
        runChecks(CheckType.MINED);
        runChecks(CheckType.ANY);
    }

    public void blockPlace(BlockTargetData blockTargetData) {
        packetDataList.add(CheckType.PLACE, null, null, false, false, true, blockTargetData);
        BaritoneRemover.scheduler().runAsync(() -> {
            runChecks(CheckType.PLACE);
            runChecks(CheckType.ANY);
        });
    }

    public boolean toggleDebug(CommandSender viewer) {
        if (!(viewer instanceof Player)) {
            boolean enabled = !debugConsole.get();
            debugConsole.set(enabled);
            return enabled;
        }
        String viewerName = viewer.getName();
        if (debugPlayers.remove(viewerName)) {
            return false;
        }
        debugPlayers.add(viewerName);
        return true;
    }

    public void sendDebug(Component message) {
        if (debugConsole.get()) {
            ConfigManager.getInstance().adventure().sender(Bukkit.getConsoleSender()).sendMessage(message);
        }
        for (String viewerName : new HashSet<>(debugPlayers)) {
            Player viewer = Bukkit.getPlayerExact(viewerName);
            if (viewer == null) {
                debugPlayers.remove(viewerName);
                continue;
            }
            ConfigManager.getInstance().adventure().player(viewer).sendMessage(message);
        }
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
        if (shouldDisableChecksForPing(player)) {
            resetPacketDataIfOverCapacity();
            return;
        }
        if (player != null && player.hasPermission("br.bypass") && !isDebug()) {
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
        resetPacketDataIfOverCapacity();
    }

    private void resetPacketDataIfOverCapacity() {
        if (packetDataList.size() > 1000) {
            packetDataList = new PacketDatas();
        }
    }

    private boolean shouldDisableChecksForPing(Player player) {
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.pingDisableEnabled() || player == null) {
            return false;
        }
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        return user != null && PacketEvents.getAPI().getPlayerManager().getPing(player) >= configManager.pingDisableThreshold();
    }

    public boolean isDebug() {
        return debugConsole.get() || !debugPlayers.isEmpty();
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
