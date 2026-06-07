package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.DistinctTargetTracker;
import me.chrommob.baritoneremover.checks.inter.SustainedMiningPattern;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import me.chrommob.baritoneremover.data.types.PacketData;
import me.chrommob.baritoneremover.data.types.RotationData;

@CheckData(name = "TargetLock", identifier = "C", checkType = CheckType.MINED,
        description = "Checks for Baritone's fixed 26-degree traverse-mining pitch with sustained yaw jitter")
public final class TargetLockC extends Check {
    private static final long MIN_MINING_DURATION_MILLIS = 150L;
    private static final long MAX_MINING_DURATION_MILLIS = 10_000L;
    private static final int REQUIRED_DISTINCT_TARGETS = 4;
    private static final int FLAG_AMOUNT = 5;
    private final DistinctTargetTracker targetTracker = new DistinctTargetTracker(90_000L);

    public TargetLockC(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        if (playerData.isBedrock()) {
            return;
        }
        PacketDatas packets = playerData.packetDataList();
        PacketData finish = packets.getLatest(CheckType.MINED);
        if (finish == null) {
            return;
        }
        PacketData start = packets.getPrevious(finish, CheckType.MINING);
        if (start == null || start.blockTargetData() == null) {
            return;
        }
        long duration = finish.timeStamp() - start.timeStamp();
        if (duration < MIN_MINING_DURATION_MILLIS || duration > MAX_MINING_DURATION_MILLIS) {
            return;
        }

        BlockTargetData target = start.blockTargetData();
        int samples = 0;
        double yawMovement = 0.0;
        double pitchMovement = 0.0;
        double pitchSum = 0.0;
        double minimumPitch = Double.MAX_VALUE;
        double maximumPitch = -Double.MAX_VALUE;
        RotationData previousRotation = null;
        for (int index = start.index() + 1; index < finish.index(); index++) {
            PacketData packet = packets.get(index);
            if (packet.rotationData() == null) {
                continue;
            }
            RotationData rotation = packet.rotationData();
            samples++;
            pitchSum += rotation.pitch();
            minimumPitch = Math.min(minimumPitch, rotation.pitch());
            maximumPitch = Math.max(maximumPitch, rotation.pitch());
            if (previousRotation != null) {
                yawMovement += rotation.differenceYaw(previousRotation);
                pitchMovement += rotation.differencePitch(previousRotation);
            }
            previousRotation = rotation;
        }

        double averagePitch = samples == 0 ? 0.0 : pitchSum / samples;
        double pitchSpread = samples == 0 ? 0.0 : maximumPitch - minimumPitch;
        boolean sustainedLock = SustainedMiningPattern.matches(samples, yawMovement, pitchMovement, pitchSpread,
                averagePitch);
        if (!sustainedLock) {
            if (playerData.isDebug()) {
                debug("traverse candidate rejected: samples=" + samples + " yawMovement=" + yawMovement
                        + " pitchMovement=" + pitchMovement + " pitchSpread=" + pitchSpread + " averagePitch="
                        + averagePitch);
            }
            return;
        }

        int distinctTargets = targetTracker.record(target.key(), finish.timeStamp());
        if (distinctTargets == 0) {
            return;
        }
        debug("traverse target evidence: samples=" + samples + " yawMovement=" + yawMovement + " pitchMovement="
                + pitchMovement + " pitchSpread=" + pitchSpread + " averagePitch=" + averagePitch + " targets="
                + distinctTargets);
        if (distinctTargets >= REQUIRED_DISTINCT_TARGETS) {
            increaseVl(FLAG_AMOUNT);
        }
    }

}
