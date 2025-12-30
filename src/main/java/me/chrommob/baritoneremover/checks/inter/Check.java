package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;

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
    private final boolean hidden;
    public Check(PlayerData playerData) {
        boolean annotationPresent = this.getClass().isAnnotationPresent(CheckData.class);
        this.name = annotationPresent ? this.getClass().getAnnotation(CheckData.class).name() : "Unknown";
        this.identifier = annotationPresent ? this.getClass().getAnnotation(CheckData.class).identifier() : "Unknown";
        this.description = annotationPresent ? this.getClass().getAnnotation(CheckData.class).description() : "Unknown";
        this.checkType = annotationPresent ? this.getClass().getAnnotation(CheckData.class).checkType() : CheckType.NONE;
        if (!ConfigManager.getInstance().getConfigData(this.getClass()).enable()) {
            checkType = CheckType.NONE;
        }
        this.hidden = annotationPresent && this.getClass().getAnnotation(CheckData.class).hidden();
        this.punishVl = ConfigManager.getInstance().getConfigData(this.getClass()).punishVl();
        this.punishment = ConfigManager.getInstance().getConfigData(this.getClass()).punishCommand().replace("%player%", playerData.name());
        this.punish = ConfigManager.getInstance().getConfigData(this.getClass()).punish();
        this.playerData = playerData;
        this.playerName = playerData.name();
        BaritoneRemover.scheduler().runTimer(() -> {
            if (System.currentTimeMillis() - latestFlag > 1000 * 20) {
                currentVl = currentVl > 0 ? currentVl - 1 : 0;
            }
        }, 0, 20 * 10);
    }

    private int currentVl = 0;
    private long latestFlag = System.currentTimeMillis();

    public abstract void run();

    public void increaseVl(int amount) {
        if (hidden) {
            return;
        }
        latestFlag = System.currentTimeMillis();
        currentVl += amount;
        if (currentVl == 0) {
            return;
        }
        alert();
        if (currentVl >= punishVl) {
            currentVl = 0;
            punish();
        }
    }

    public void resetVl() {
        currentVl = 0;
    }

    private void alert() {
        int currentVl = this.currentVl;
        Component message = ConfigManager.getInstance().punishmentMessage()
                .replaceText(TextReplacementConfig.builder().matchLiteral("{prefix}").replacement(ConfigManager.getInstance().prefix()).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("{player}").replacement(playerName).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("{check}").replacement(name + " (" + identifier + ")").build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("{vl}").replacement(String.valueOf(currentVl)).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("{punish-vl}").replacement(String.valueOf(punishVl)).build())
                .hoverEvent(Component.text(description).color(NamedTextColor.AQUA));
        ConfigManager.getInstance().adventure().permission("br.alert").sendMessage(message);
        String messageString = PlainTextComponentSerializer.plainText().serialize(message);
        ConfigManager.getInstance().appendDebug(messageString);
        String json = "{\"content\": \"Player " + playerName + " has been flagged for " + name + " (" + identifier + ") (VL: " + currentVl + "/" + punishVl + ")\"}";
        ConfigManager.getInstance().sender().add(json, playerName,currentVl >= punishVl);
    }

    public void debug(String text) {
        String message = "Debug: " + playerName + " " + name + " (" + identifier + ") " + text;
        ConfigManager.getInstance().appendDebug(message);
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
        BaritoneRemover.scheduler().run(() -> BaritoneRemover.getPlugin(BaritoneRemover.class).getServer().dispatchCommand(BaritoneRemover.getPlugin(BaritoneRemover.class).getServer().getConsoleSender(), punishment));
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


