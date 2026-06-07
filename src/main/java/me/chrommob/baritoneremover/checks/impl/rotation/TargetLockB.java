package me.chrommob.baritoneremover.checks.impl.rotation;

import me.chrommob.baritoneremover.checks.inter.CanonicalTargetLockCheck;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;

@CheckData(name = "TargetLock", identifier = "B", checkType = CheckType.PLACE,
        description = "Checks for repeated one-packet snaps to Baritone's pathing placement target")
public final class TargetLockB extends CanonicalTargetLockCheck {
    public TargetLockB(PlayerData playerData) {
        super(playerData);
    }
}
