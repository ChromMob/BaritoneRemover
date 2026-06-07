package me.chrommob.baritoneremover.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
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
    @CommandCompletion("@players")
    @Syntax("[player]")
    public void onDebug(CommandSender sender, @Optional String playerName) {
        Player target;
        if (playerName == null || playerName.isEmpty()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Usage: /br debug <player>");
                return;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayerExact(playerName);
            if (target == null) {
                sender.sendMessage("Player '" + playerName + "' is not online.");
                return;
            }
        }
        PlayerData pd = dataHolder.getPlayerData(target.getName());
        boolean enabled = pd.toggleDebug(sender);
        ConfigManager.getInstance().adventure().sender(sender).sendMessage(ConfigManager.getInstance().prefix()
                .append(Component.text("Debug mode for " + target.getName() + " "
                        + (enabled ? "enabled!" : "disabled!"))
                        .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));
        if (enabled && target.hasPermission("br.bypass")) {
            ConfigManager.getInstance().adventure().sender(sender).sendMessage(ConfigManager.getInstance().prefix()
                    .append(Component.text(target.getName()
                            + " has br.bypass; debug mode will temporarily run checks anyway.")
                            .color(NamedTextColor.YELLOW)));
        }
    }

}
