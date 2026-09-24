package me.chrommob.baritoneremover.checks.inter;

import me.chrommob.baritoneremover.checks.impl.rotation.TargetLockA;
import me.chrommob.baritoneremover.checks.impl.rotation.TargetLockD;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ChecksTest {
    @Test
    public void preciseMiningChecksAreRegistered() {
        Checks checks = new Checks(null);
        assertTrue(checks.getChecks().contains(TargetLockA.class));
        assertTrue(checks.getChecks().contains(TargetLockD.class));
    }
}
