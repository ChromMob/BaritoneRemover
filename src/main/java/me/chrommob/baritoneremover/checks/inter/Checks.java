package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.impl.autowalk.AutoWalkA;
import me.chrommob.baritoneremover.checks.impl.rotation.TimeBetweenA;
import me.chrommob.baritoneremover.checks.impl.rotation.TimeBetweenB;

import java.util.*;

public class Checks {
    private final BaritoneRemover plugin;
    private final Set<Class<? extends Check>> checks = new HashSet<>();
    public Checks(BaritoneRemover pl) {
        this.plugin = pl;
        registerChecks();
    }

    private void registerChecks() {
        checks.add(AutoWalkA.class);
        checks.add(TimeBetweenA.class);
        checks.add(TimeBetweenB.class);
    }

    public Set<Class<? extends Check>> getChecks() {
        return checks;
    }
}
