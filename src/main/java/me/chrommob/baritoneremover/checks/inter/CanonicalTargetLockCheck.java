package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.PlayerData;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import me.chrommob.baritoneremover.data.types.PacketData;

public abstract class CanonicalTargetLockCheck extends Check {
    private static final int FLAG_AMOUNT = 5;
    private final long maxActionDelayMillis;
    private final long rotationHistoryMillis;
    private final int requiredDistinctTargets;
    private final DistinctTargetTracker targetTracker;

    protected CanonicalTargetLockCheck(PlayerData playerData) {
        this(playerData, 8, 45_000L, 150L, 500L);
    }

    protected CanonicalTargetLockCheck(PlayerData playerData, int requiredDistinctTargets, long targetWindowMillis,
            long maxActionDelayMillis, long rotationHistoryMillis) {
        super(playerData);
        this.requiredDistinctTargets = requiredDistinctTargets;
        this.maxActionDelayMillis = maxActionDelayMillis;
        this.rotationHistoryMillis = rotationHistoryMillis;
        this.targetTracker = new DistinctTargetTracker(targetWindowMillis);
    }

    @Override
    public final void run() {
        if (playerData.isBedrock()) {
            return;
        }
        PacketDatas packets = playerData.packetDataList();
        PacketData action = packets.getLatest(checkType());
        if (action == null) {
            return;
        }
        BlockTargetData target = action.blockTargetData();
        if (target == null || (target.placement() && !target.hasValidFace())
                || (!target.includeBlockCenter() && !target.hasValidFace())) {
            return;
        }
        PacketData currentRotation = packets.getPrevious(action, CheckType.ROTATION, CheckType.FLYING);
        PacketData position = packets.getPrevious(action, CheckType.POSITION, CheckType.FLYING);
        if (currentRotation == null || position == null) {
            if (playerData.isDebug()) {
                debug("candidate rejected: missing rotation or position");
            }
            return;
        }
        long actionDelay = action.timeStamp() - currentRotation.timeStamp();
        if (actionDelay < 0L || actionDelay > maxActionDelayMillis) {
            if (playerData.isDebug()) {
                debug("candidate rejected: actionDelay=" + actionDelay + " maxActionDelay=" + maxActionDelayMillis);
            }
            return;
        }

        CanonicalTargeting.Result result = null;
        CanonicalTargeting.Result diagnostic = null;
        PacketData cursor = currentRotation;
        while (true) {
            PacketData previousRotation = packets.getPrevious(cursor, CheckType.ROTATION, CheckType.FLYING);
            if (previousRotation == null
                    || currentRotation.timeStamp() - previousRotation.timeStamp() > rotationHistoryMillis) {
                break;
            }
            CanonicalTargeting.Result candidate = CanonicalTargeting.evaluate(position.positionData(),
                    currentRotation.rotationData(), previousRotation.rotationData(), target);
            if (diagnostic == null || candidate.previousError() > diagnostic.previousError()) {
                diagnostic = candidate;
            }
            if (isEvidence(candidate) && (result == null || candidate.previousError() > result.previousError())) {
                result = candidate;
            }
            cursor = previousRotation;
        }
        if (result == null) {
            if (playerData.isDebug() && diagnostic != null) {
                debug("candidate rejected: error=" + diagnostic.currentError() + " yawError="
                        + diagnostic.currentYawError() + " pitchError=" + diagnostic.currentPitchError()
                        + " previousError=" + diagnostic.previousError() + " actionDelay=" + actionDelay);
            } else if (playerData.isDebug()) {
                debug("candidate rejected: no rotation history within " + rotationHistoryMillis + "ms");
            }
            return;
        }
        int distinctTargets = targetTracker.record(target.key(), action.timeStamp());
        if (distinctTargets == 0) {
            return;
        }
        debug("target evidence: error=" + result.currentError() + " yawError=" + result.currentYawError()
                + " pitchError=" + result.currentPitchError() + " previousError=" + result.previousError()
                + " targets=" + distinctTargets);
        if (distinctTargets < requiredDistinctTargets) {
            return;
        }
        increaseVl(FLAG_AMOUNT);
    }

    protected boolean isEvidence(CanonicalTargeting.Result result) {
        return result.isCanonicalSnap();
    }
}
