package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.PacketData;

import java.util.OptionalDouble;

public abstract class CanonicalMiningTargetCheck extends Check {
    private static final int FLAG_AMOUNT = 5;
    private final long maxActionDelayMillis;
    private final MiningAimTracker tracker;

    protected CanonicalMiningTargetCheck(PlayerData playerData, int requiredMatches, int maxTargets,
            long targetWindowMillis, long maxActionDelayMillis) {
        super(playerData);
        this.maxActionDelayMillis = maxActionDelayMillis;
        this.tracker = new MiningAimTracker(requiredMatches, maxTargets, targetWindowMillis);
    }

    @Override
    public final void run() {
        if (playerData.isBedrock()) {
            return;
        }
        PacketDatas packets = playerData.packetDataList();
        PacketData action = packets.getLatest(CheckType.MINING);
        if (action == null || action.blockTargetData() == null) {
            return;
        }
        CanonicalTargeting.Result result = MiningTargeting.evaluate(packets, action, maxActionDelayMillis);
        boolean match = result != null && isEvidence(result);
        if (match) {
            debug("precise mining aim: pitchError=" + result.currentPitchError() + " yawError="
                    + result.currentYawError() + " previousError=" + result.previousError()
                    + " target=" + action.blockTargetData().key());
        }
        OptionalDouble pitch = match ? OptionalDouble.of(result.expectedPitch()) : OptionalDouble.empty();
        if (tracker.record(action.blockTargetData().key(), pitch, action.timeStamp())) {
            increaseVl(FLAG_AMOUNT);
        }
    }

    protected abstract boolean isEvidence(CanonicalTargeting.Result result);
}
