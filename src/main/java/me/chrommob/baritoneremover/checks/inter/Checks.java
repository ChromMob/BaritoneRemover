package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.BaritoneRemover;
import me.chrommob.baritoneremover.checks.impl.movement.AutoWalkA;
import me.chrommob.baritoneremover.checks.impl.general.CombinedA;
import me.chrommob.baritoneremover.checks.impl.movement.RepeatedB;
import me.chrommob.baritoneremover.checks.impl.movement.RepeatedD;
import me.chrommob.baritoneremover.checks.impl.rotation.*;

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
        checks.add(RepeatedB.class);
        checks.add(RepeatedD.class);

        checks.add(Cinematic.class);
        checks.add(RepeatedA.class);
        checks.add(RepeatedC.class);
        checks.add(TimeBetweenA.class);
        checks.add(TimeBetweenB.class);

        checks.add(CombinedA.class);
    }

    public Set<Class<? extends Check>> getChecks() {
        return checks;
    }
}
