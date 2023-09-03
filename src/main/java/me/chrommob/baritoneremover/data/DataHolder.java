package me.chrommob.baritoneremover.data;

import me.chrommob.baritoneremover.BaritoneRemover;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataHolder {
    private final Map<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final BaritoneRemover plugin;

    public DataHolder(BaritoneRemover plugin) {
        this.plugin = plugin;
    }

    public PlayerData getPlayerData(String name) {
        return playerDataMap.computeIfAbsent(name, s -> new PlayerData(name, plugin.checks()));
    }

    public void removePlayerData(String name) {
        playerDataMap.remove(name);
    }

    public void clear() {
        playerDataMap.clear();
    }
}
