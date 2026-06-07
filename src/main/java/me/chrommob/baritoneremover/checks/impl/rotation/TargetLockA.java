package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.CanonicalTargetLockCheck;
import me.chrommob.baritoneremover.checks.inter.CanonicalTargeting;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;

@CheckData(name = "TargetLock", identifier = "A", checkType = CheckType.MINING,
        description = "Checks for repeated Baritone-style acquisition of canonical mining targets")
public final class TargetLockA extends CanonicalTargetLockCheck {
    public TargetLockA(PlayerData playerData) {
        super(playerData, 6, 60_000L, 250L, 1_500L);
    }

    @Override
    protected boolean isEvidence(CanonicalTargeting.Result result) {
        return result.isBaritoneMiningApproach();
    }
}
