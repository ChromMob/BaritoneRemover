package me.chrommob.baritoneremover.checks.impl.general;

import me.chrommob.baritoneremover.checks.inter.Check;
import me.chrommob.baritoneremover.checks.inter.CheckData;
import me.chrommob.baritoneremover.checks.inter.CheckType;
import me.chrommob.baritoneremover.data.PlayerData;

import java.util.HashSet;
import java.util.Set;

@CheckData(name = "Combined", identifier = "A", description = "Checks for combined violations", checkType = CheckType.ANY)
public class Combined extends Check {

    public Combined(PlayerData playerData) {
        super(playerData);
    }

    @Override
    public void run(CheckType updateType) {
        resetVl();
        Set<Check> checks = new HashSet<>(playerData.checks());
        if (checks.isEmpty()) {
            return;
        }
        int violations = 0;
        for (Check check : checks) {
            violations += check.currentVl();
        }
        if (violations < punishVl()) {
            return;
        }
        increaseVl(violations);
    }
}