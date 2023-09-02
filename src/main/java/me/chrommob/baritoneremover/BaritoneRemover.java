package me.chrommob.baritoneremover;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.chrommob.baritoneremover.checks.inter.Checks;
import me.chrommob.baritoneremover.config.ConfigManager;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.listener.DisconnectListener;
import me.chrommob.baritoneremover.listener.MiningListener;
import me.chrommob.baritoneremover.listener.RotationListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BaritoneRemover extends JavaPlugin {
    private DataHolder dataHolder;
    private Checks checks;
    private ConfigManager configManager;
    private File configFile;

    @Override
    public void onLoad() {
        File dataFolder = getDataFolder();
        configFile = new File(dataFolder, "config.yml");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        //Are all listeners read only?
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(true);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        new Metrics(this, 19704);
        dataHolder = new DataHolder(this);
        checks = new Checks(this);
        configManager = new ConfigManager(this);
        PacketEvents.getAPI().getEventManager().registerListener(new RotationListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new DisconnectListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new MiningListener(this));
        PacketEvents.getAPI().init();
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
}
