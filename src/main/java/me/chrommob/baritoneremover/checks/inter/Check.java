package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public abstract class Check {
    private final CheckType checkType;
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
        this.punishVl = ConfigManager.getInstance().getConfigData(this.getClass()).punishVl();
        this.punishment = ConfigManager.getInstance().getConfigData(this.getClass()).punishCommand().replace("%player%", playerData.name());
        this.punish = ConfigManager.getInstance().getConfigData(this.getClass()).punish();
        this.playerData = playerData;
        this.playerName = playerData.name();
    }

    private double currentVl = 0;
    private long latestFlag = System.currentTimeMillis();

    public abstract void run(CheckType updateType);

    public void increaseVl(int amount) {
        latestFlag = System.currentTimeMillis();
        currentVl += amount;
        alert();
        if (currentVl >= punishVl) {
            punish();
        }
    }

    private void alert() {
        ConfigManager.getInstance().adventure().permission("br.alert").sendMessage(Component.text("[").color(NamedTextColor.WHITE)
                .append(Component.text("BaritoneRemover").color(NamedTextColor.RED))
                .append(Component.text("] ").color(NamedTextColor.WHITE))
                .append(Component.text("Player ").color(NamedTextColor.WHITE))
                .append(Component.text(playerName).color(NamedTextColor.RED))
                .append(Component.text(" has been flagged for ").color(NamedTextColor.WHITE))
                .append(Component.text(name + " (" + identifier + ")").color(NamedTextColor.RED))
                .append(Component.text(" (VL: " + currentVl + "/" + punishVl + ")").color(NamedTextColor.WHITE)));
        Bukkit.getLogger().info("Player " + playerName + " has been flagged for " + name + " (" + identifier + ") (VL: " + currentVl + "/" + punishVl + ")");
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
}


