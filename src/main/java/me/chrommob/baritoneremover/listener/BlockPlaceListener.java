package me.chrommob.baritoneremover.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;

public class BlockPlaceListener extends SimplePacketListenerAbstract {
    private final DataHolder dataHolder;
    public BlockPlaceListener(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT)) {
            return;
        }
        PlayerData pd = dataHolder.getPlayerData(event.getUser().getName());
        pd.blockPlace();
    }
}
