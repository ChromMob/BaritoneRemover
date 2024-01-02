package me.chrommob.baritoneremover.listener;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    private final DataHolder dataHolder;

    public BlockPlaceListener(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = dataHolder.getPlayerData(player.getName());
        playerData.blockPlace();
    }
}
