package me.chrommob.baritoneremover.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;

public class RotationListener extends SimplePacketListenerAbstract {
    private final DataHolder dataHolder;

    public RotationListener(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @Override
    public void onPacketPlayReceive(final PacketPlayReceiveEvent event) {
        boolean flying = WrapperPlayClientPlayerFlying.isFlying(event.getPacketType());
        if (!flying)
            return;
        WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
        if (!packet.hasPositionChanged() && !packet.hasRotationChanged())
            return;
        PlayerData pd = dataHolder.getPlayerData(event.getUser().getName());
        if (pd == null)
            return;
        if (packet.hasPositionChanged() && packet.hasRotationChanged()) {
            pd.updateBoth(packet.getLocation().getPosition(), packet.getLocation().getPitch(),
                    packet.getLocation().getYaw());
            return;
        }
        if (packet.hasPositionChanged() && (pd.packetDataList().getLatest(CheckType.POSITION) == null
                || pd.packetDataList().getLatest(CheckType.POSITION).positionData()
                        .distance(packet.getLocation().getPosition()) > 0)) {
            pd.updatePosition(packet.getLocation().getPosition());
            return;
        }
        if (packet.hasRotationChanged()) {
            pd.updateRotation(packet.getLocation().getPitch(), packet.getLocation().getYaw());
        }
    }
}
