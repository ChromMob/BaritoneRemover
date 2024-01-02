package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.*;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.RotationData;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "Cinematic", identifier = "A", checkType = CheckType.ROTATION, description = "Checks if the player is rotating in a cinematic way")
public class Cinematic extends Check {
    private long lastSmooth = 0L, lastHighRate = 0L;
    private double lastDeltaYaw = 0.0d, lastDeltaPitch = 0.0d;

    private final List<Double> yawSamples = new ArrayList<>();
    private final List<Double> pitchSamples = new ArrayList<>();
    public Cinematic(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();

        if (playerData.packetDataList().size(CheckType.ROTATION) < 2) {
            return;
        }
        PacketData packetData = playerData.packetDataList().getLatest(CheckType.ROTATION, CheckType.FLYING);
        PacketData previous = playerData.packetDataList().getPrevious(packetData, CheckType.ROTATION, CheckType.FLYING);
        RotationData rotationUpdate = packetData.rotationData();
        RotationData previousRotation = previous.rotationData();
        final double deltaYaw = rotationUpdate.differenceYaw(previousRotation);
        final double deltaPitch = rotationUpdate.differencePitch(previousRotation);

        final double differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final double differencePitch = Math.abs(deltaPitch - lastDeltaPitch);

        final double joltYaw = Math.abs(differenceYaw - deltaYaw);
        final double joltPitch = Math.abs(differencePitch - deltaPitch);

        final boolean cinematic = (now - lastHighRate > 250L) || now - lastSmooth < 9000L;

        if (joltYaw > 1.0 && joltPitch > 1.0) {
            this.lastHighRate = now;
        }

        if (deltaYaw > 0.0 && deltaPitch > 0.0) {
            yawSamples.add(deltaYaw);
            pitchSamples.add(deltaPitch);
        }

        if (yawSamples.size() == 20 && pitchSamples.size() == 20) {
            // Get the cerberus/positive graph of the sample-lists
            final GraphResult resultsYaw = Utils.getGraph(yawSamples);
            final GraphResult resultsPitch = Utils.getGraph(pitchSamples);

            // Negative values
            final int negativesYaw = resultsYaw.negatives;
            final int negativesPitch = resultsPitch.negatives;

            // Positive values
            final int positivesYaw = resultsYaw.positives;
            final int positivesPitch = resultsPitch.positives;

            // Cinematic camera usually does this on *most* speeds and is accurate for the most part.
            if (positivesYaw > negativesYaw || positivesPitch > negativesPitch) {
                this.lastSmooth = now;
            }

            yawSamples.clear();
            pitchSamples.clear();
        }

        playerData.setCinematic(cinematic);

        this.lastDeltaYaw = deltaYaw;
        this.lastDeltaPitch = deltaPitch;
    }
}
