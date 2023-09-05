package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public abstract class Check {
    private CheckType checkType;
    private final String name;
    private final String identifier;
    private final String description;
    private final int punishVl;
    public final PlayerData playerData;
    private final String playerName;
    private final boolean punish;
    private final String punishment;
    public Check(PlayerData playerData) {
        boolean annotationPresent = this.getClass().isAnnotationPresent(CheckData.class);
        this.name = annotationPresent ? this.getClass().getAnnotation(CheckData.class).name() : "Unknown";
        this.identifier = annotationPresent ? this.getClass().getAnnotation(CheckData.class).identifier() : "Unknown";
        this.description = annotationPresent ? this.getClass().getAnnotation(CheckData.class).description() : "Unknown";
        this.checkType = annotationPresent ? this.getClass().getAnnotation(CheckData.class).checkType() : CheckType.NONE;
        if (!ConfigManager.getInstance().getConfigData(this.getClass()).enable()) {
            checkType = CheckType.NONE;
        }
        this.punishVl = ConfigManager.getInstance().getConfigData(this.getClass()).punishVl();
        this.punishment = ConfigManager.getInstance().getConfigData(this.getClass()).punishCommand().replace("%player%", playerData.name());
        this.punish = ConfigManager.getInstance().getConfigData(this.getClass()).punish();
        this.playerData = playerData;
        this.playerName = playerData.name();
        Bukkit.getScheduler().runTaskTimer(BaritoneRemover.getPlugin(BaritoneRemover.class), () -> {
            if (System.currentTimeMillis() - latestFlag > 1000 * 20) {
                currentVl = currentVl > 0 ? currentVl - 1 : 0;
            }
        }, 0, 20 * 10);
    }

    private int currentVl = 0;
    private long latestFlag = System.currentTimeMillis();

    public abstract void run(CheckType updateType);

    public void increaseVl(int amount) {
        latestFlag = System.currentTimeMillis();
        currentVl += amount;
        if (currentVl == 0) {
            return;
        }
        alert();
        if (currentVl >= punishVl) {
            punish();
            currentVl = 0;
        }
    }

    public void resetVl() {
        currentVl = 0;
    }

    private void alert() {
        int currentVl = this.currentVl;
        ConfigManager.getInstance().adventure().permission("br.alert").sendMessage(
                ConfigManager.getInstance().prefix()
                .append(Component.text("Player ").color(NamedTextColor.WHITE))
                .append(Component.text(playerName).color(NamedTextColor.RED))
                .append(Component.text(" has been flagged for ").color(NamedTextColor.WHITE))
                .append(Component.text(name + " (" + identifier + ")").color(NamedTextColor.RED))
                .append(Component.text(" (VL: " + currentVl + "/" + punishVl + ")").color(NamedTextColor.WHITE)).hoverEvent(Component.text(description).color(NamedTextColor.AQUA)));
        boolean webHookEnabled = ConfigManager.getInstance().webHookEnabled();
        if (!webHookEnabled) {
            return;
        }
        String webHookUrl = ConfigManager.getInstance().webHookUrl();
        Bukkit.getScheduler().runTaskAsynchronously(BaritoneRemover.getPlugin(BaritoneRemover.class), () -> {
                try {
                    URLConnection connection = new URL(webHookUrl).openConnection();
                    connection.setDoOutput(true);
                    ((HttpsURLConnection) connection).setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    String json = "{\"content\": \"Player " + playerName + " has been flagged for " + name + " (" + identifier + ") (VL: " + currentVl + "/" + punishVl + ")\"}";
                    OutputStream stream = connection.getOutputStream();
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    stream.write(bytes, 0, bytes.length);
                    stream.close();
                    connection.getInputStream().close();
                } catch (IOException ignored) {
                }
        });
    }

    public void debug(String text) {
        if (playerData.isDebug()) {
            ConfigManager.getInstance().adventure().player(Bukkit.getPlayer(playerName)).sendMessage(
                    ConfigManager.getInstance().prefix()
                            .append(Component.text("Debug: ").color(NamedTextColor.WHITE))
                            .append(Component.text(text).color(NamedTextColor.RED)));
        }
    }

    private void punish() {
        if (!punish) {
            return;
        }
        Bukkit.getServer().getScheduler().runTask(BaritoneRemover.getPlugin(BaritoneRemover.class), () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), punishment));
    }

    public CheckType checkType() {
        return checkType;
    }

    public int currentVl() {
        return currentVl;
    }

    public int punishVl() {
        return punishVl;
    }
}


