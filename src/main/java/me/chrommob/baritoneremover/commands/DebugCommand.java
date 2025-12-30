package me.chrommob.baritoneremover.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public void onDebug(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return;
        }
        PlayerData pd = dataHolder.getPlayerData(sender.getName());
        pd.debug();
        ConfigManager.getInstance().adventure().player((Player) sender)
                .sendMessage(ConfigManager.getInstance().prefix()
                        .append(pd.isDebug() ? Component.text("Debug mode enabled!").color(NamedTextColor.GREEN)
                                : Component.text("Debug mode disabled!").color(NamedTextColor.RED)));
    }

}
