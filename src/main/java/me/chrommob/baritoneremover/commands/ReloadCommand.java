package me.chrommob.baritoneremover.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@CommandAlias("baritoneremover|br")
@CommandPermission("br.reload")
public class ReloadCommand extends BaseCommand {
    private final BaritoneRemover plugin;
    public ReloadCommand(BaritoneRemover pl) {
        this.plugin = pl;
    }
    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        ConfigManager.getInstance().adventure().sender(sender).sendMessage(ConfigManager.getInstance().prefix().append(Component.text("Reloading...").color(NamedTextColor.GREEN)));
        plugin.reload();
    }
}
