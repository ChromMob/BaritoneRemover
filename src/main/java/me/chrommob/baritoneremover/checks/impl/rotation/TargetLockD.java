package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.CanonicalMiningTargetCheck;
import me.chrommob.baritoneremover.checks.inter.CanonicalTargeting;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;

@CheckData(name = "TargetLock", identifier = "D", checkType = CheckType.MINING,
        description = "Checks for repeated precise aim held on distinct mining targets")
public final class TargetLockD extends CanonicalMiningTargetCheck {
    public TargetLockD(PlayerData playerData) {
        super(playerData, 12, 15, 90_000L, 300L);
    }

    @Override
    protected boolean isEvidence(CanonicalTargeting.Result result) {
        return result.isPreciseMiningHold();
    }
}
