package me.chrommob.baritoneremover.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.data.DataHolder;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import org.bukkit.entity.Player;

public class MiningListener extends SimplePacketListenerAbstract {
    private final DataHolder dataHolder;

    public MiningListener(BaritoneRemover pl) {
        this.dataHolder = pl.dataHolder();
    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Client.PLAYER_DIGGING)) {
            return;
        }
        WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
        if (packet.getBlockFace() == null) {
            return;
        }
        if (packet.getAction() != DiggingAction.START_DIGGING && packet.getAction() != DiggingAction.FINISHED_DIGGING) {
            return;
        }
        PlayerData pd = dataHolder.getPlayerData(event.getUser().getName());
        if (pd == null) {
            return;
        }
        if (packet.getAction() == DiggingAction.START_DIGGING) {
            Vector3i position = packet.getBlockPosition();
            BlockFace face = packet.getBlockFace();
            Player player = event.getPlayer();
            double eyeHeight = player == null ? 1.62 : player.getEyeHeight();
            pd.startMining(BlockTargetData.mining(position.x, position.y, position.z, face.getModX(), face.getModY(),
                    face.getModZ(), eyeHeight));
            return;
        }
        if (packet.getAction() == DiggingAction.FINISHED_DIGGING) {
            pd.finishMining();
        }
    }
}
