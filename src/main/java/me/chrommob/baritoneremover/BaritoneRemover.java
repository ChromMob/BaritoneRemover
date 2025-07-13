package me.chrommob.baritoneremover;

import co.aikar.commands.PaperCommandManager;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.commands.DebugCommand;
import me.chrommob.baritoneremover.commands.ReloadCommand;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.Scheduler;
import me.chrommob.baritoneremover.listener.BlockPlaceListener;
import me.chrommob.baritoneremover.listener.DisconnectListener;
import me.chrommob.baritoneremover.listener.MiningListener;
import me.chrommob.baritoneremover.listener.RotationListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BaritoneRemover extends JavaPlugin {
    private DataHolder dataHolder;
    private Checks checks;
    private ConfigManager configManager;
    private File configFile;
    private File debugFolder;
    private static Scheduler scheduler;

    public static Scheduler scheduler() {
        return scheduler;
    }

    @Override
    public void onLoad() {
        scheduler = new Scheduler(this);
        File dataFolder = getDataFolder();
        configFile = new File(dataFolder, "config.yml");
        debugFolder = new File(dataFolder, "debug");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        if (!debugFolder.exists()) {
            debugFolder.mkdirs();
        }
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        // Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        new Metrics(this, 19704);
        dataHolder = new DataHolder(this);
        checks = new Checks(this);
        configManager = new ConfigManager(this);

        commandManager.registerCommand(new DebugCommand(this));
        commandManager.registerCommand(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);

        PacketEvents.getAPI().getEventManager().registerListener(new RotationListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new DisconnectListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new MiningListener(this));
        PacketEvents.getAPI().init();
    }

    public void reload() {
        configManager.loadConfig();
        dataHolder.clear();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PacketEvents.getAPI().terminate();
    }

    public DataHolder dataHolder() {
        return dataHolder;
    }

    public Checks checks() {
        return checks;
    }

    public File configFile() {
        return configFile;
    }

    public File debugFolder() {
        return debugFolder;
    }
}
