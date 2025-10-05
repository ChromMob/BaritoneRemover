package me.chrommob.baritoneremover.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("br|baritoneremover")
@CommandPermission("br.debug")
public class DebugCommand extends BaseCommand {
    public DataHolder dataHolder;

    public DebugCommand(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @Subcommand("debug")
    @Syntax("[player]")
    @Description("Toggle debug mode for yourself or a specified player")
    public void onDebug(CommandSender sender, @Optional String targetPlayerName) {
        String playerName;
        
        // Determine which player to toggle debug for
        if (targetPlayerName == null || targetPlayerName.isEmpty()) {
            // No player specified
            if (!(sender instanceof Player)) {
                ConfigManager.getInstance().adventure().sender(sender)
                        .sendMessage(ConfigManager.getInstance().prefix()
                                .append(Component.text("You must specify a player name from console!").color(NamedTextColor.RED)));
                return;
            }
            playerName = sender.getName();
        } else {
            // Player specified
            playerName = targetPlayerName;
            
            // Check if player exists online
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                ConfigManager.getInstance().adventure().sender(sender)
                        .sendMessage(ConfigManager.getInstance().prefix()
                                .append(Component.text("Player '" + playerName + "' is not online!").color(NamedTextColor.RED)));
                return;
            }
            playerName = targetPlayer.getName(); // Use exact name
        }
        
        // Get player data and toggle debug
        PlayerData pd = dataHolder.getPlayerData(playerName);
        if (pd == null) {
            ConfigManager.getInstance().adventure().sender(sender)
                    .sendMessage(ConfigManager.getInstance().prefix()
                            .append(Component.text("No data found for player '" + playerName + "'!").color(NamedTextColor.RED)));
            return;
        }
        
        // Toggle debug with appropriate debugger
        if (targetPlayerName == null || targetPlayerName.isEmpty()) {
            // Self-debug
            pd.debug();
        } else {
            // Debug for another player - set sender as debugger
            String debuggerName = sender instanceof Player ? sender.getName() : "Console";
            pd.debug(debuggerName);
        }
        
        // Send message to command sender
        if (pd.isDebug()) {
            if (targetPlayerName == null || targetPlayerName.isEmpty()) {
                ConfigManager.getInstance().adventure().sender(sender)
                        .sendMessage(ConfigManager.getInstance().prefix()
                                .append(Component.text("Debug mode enabled! You will receive debug messages.", NamedTextColor.GREEN)));
            } else {
                ConfigManager.getInstance().adventure().sender(sender)
                        .sendMessage(ConfigManager.getInstance().prefix()
                                .append(Component.text("Debug mode enabled for " + playerName + "! You will receive their debug messages.", NamedTextColor.GREEN)));
            }
        } else {
            Component statusMessage = Component.text("Debug mode disabled for " + playerName + "!", NamedTextColor.RED);
            ConfigManager.getInstance().adventure().sender(sender)
                    .sendMessage(ConfigManager.getInstance().prefix().append(statusMessage));
        }
        
        // If toggling for another player, notify them too
        if (targetPlayerName != null && !targetPlayerName.isEmpty() && !sender.getName().equals(playerName)) {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer != null) {
                String debuggerName = sender instanceof Player ? sender.getName() : "Console";
                ConfigManager.getInstance().adventure().player(targetPlayer)
                        .sendMessage(ConfigManager.getInstance().prefix()
                                .append(pd.isDebug() 
                                        ? Component.text("Debug mode enabled by " + debuggerName + " (they will receive debug messages).", NamedTextColor.YELLOW)
                                        : Component.text("Debug mode disabled by " + debuggerName + ".", NamedTextColor.RED)));
            }
        }
    }

}
