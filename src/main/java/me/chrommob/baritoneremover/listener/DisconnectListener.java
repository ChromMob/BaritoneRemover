package me.chrommob.baritoneremover.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.data.DataHolder;

public class DisconnectListener extends SimplePacketListenerAbstract {
    private final DataHolder dataHolder;

    public DisconnectListener(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Server.DISCONNECT)) {
            return;
        }
        dataHolder.removePlayerData(event.getUser().getName());
    }
}
