package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.CanonicalTargeting;
import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.checks.inter.DistinctTargetTracker;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import me.chrommob.baritoneremover.data.types.PacketData;

@CheckData(name = "TargetLock", identifier = "D", checkType = CheckType.MINING,
        description = "Checks for repeated canonical aim-locks when mining distinct blocks")
public final class TargetLockD extends Check {
    private static final long MAX_ACTION_DELAY_MILLIS = 300L;
    private static final int REQUIRED_DISTINCT_TARGETS = 8;
    private static final int FLAG_AMOUNT = 5;
    private final DistinctTargetTracker targetTracker = new DistinctTargetTracker(90_000L);

    public TargetLockD(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run() {
        if (playerData.isBedrock()) {
            return;
        }
        PacketDatas packets = playerData.packetDataList();
        PacketData action = packets.getLatest(CheckType.MINING);
        if (action == null || action.blockTargetData() == null) {
            return;
        }
        PacketData rotation = packets.getPrevious(action, CheckType.ROTATION, CheckType.FLYING);
        PacketData position = packets.getPrevious(action, CheckType.POSITION, CheckType.FLYING);
        if (rotation == null || position == null) {
            return;
        }
        long actionDelay = action.timeStamp() - rotation.timeStamp();
        if (actionDelay < 0L || actionDelay > MAX_ACTION_DELAY_MILLIS) {
            return;
        }

        BlockTargetData target = action.blockTargetData();
        CanonicalTargeting.Result result = CanonicalTargeting.evaluate(position.positionData(), rotation.rotationData(),
                rotation.rotationData(), target);
        if (result == null || !result.isBaritoneMiningLock()) {
            if (playerData.isDebug() && result != null) {
                debug("start lock rejected: yawError=" + result.currentYawError() + " pitchError="
                        + result.currentPitchError() + " actionDelay=" + actionDelay);
            }
            return;
        }

        int distinctTargets = targetTracker.record(target.key(), action.timeStamp());
        if (distinctTargets == 0) {
            return;
        }
        debug("start lock evidence: yawError=" + result.currentYawError() + " pitchError="
                + result.currentPitchError() + " actionDelay=" + actionDelay + " targets=" + distinctTargets);
        if (distinctTargets >= REQUIRED_DISTINCT_TARGETS) {
            increaseVl(FLAG_AMOUNT);
        }
    }
}
