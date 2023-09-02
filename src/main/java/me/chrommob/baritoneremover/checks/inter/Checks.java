package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.impl.AutoWalk;
import me.chrommob.baritoneremover.checks.impl.TimeBetween;

import java.util.*;

public class Checks {
    private final BaritoneRemover plugin;
    private final Set<Class<? extends Check>> checks = new HashSet<>();
    public Checks(BaritoneRemover pl) {
        this.plugin = pl;
        registerChecks();
    }

    private void registerChecks() {
        checks.add(AutoWalk.class);
        checks.add(TimeBetween.class);
    }

    public Set<Class<? extends Check>> getChecks() {
        return checks;
    }
}
