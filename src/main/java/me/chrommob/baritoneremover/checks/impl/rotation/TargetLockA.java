package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.CanonicalMiningTargetCheck;
import me.chrommob.baritoneremover.checks.inter.CanonicalTargeting;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;

@CheckData(name = "TargetLock", identifier = "A", checkType = CheckType.MINING,
        description = "Checks for repeated Baritone-style acquisition of canonical mining targets")
public final class TargetLockA extends CanonicalMiningTargetCheck {
    public TargetLockA(PlayerData playerData) {
        super(playerData, 10, 12, 60_000L, 250L);
    }

    @Override
    protected boolean isEvidence(CanonicalTargeting.Result result) {
        return result.isPreciseMiningApproach();
    }
}
