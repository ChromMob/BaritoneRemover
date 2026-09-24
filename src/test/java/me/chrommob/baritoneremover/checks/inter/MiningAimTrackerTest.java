package me.chrommob.baritoneremover.checks.inter;

import org.junit.Test;

import java.util.OptionalDouble;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MiningAimTrackerTest {
    @Test
    public void eachViolationNeedsAFreshDiverseBatch() {
        MiningAimTracker tracker = new MiningAimTracker(3, 4, 60_000L);
        assertFalse(tracker.record("a", OptionalDouble.of(10.0), 0L));
        assertFalse(tracker.record("b", OptionalDouble.of(10.0), 1_000L));
        assertFalse(tracker.record("c", OptionalDouble.of(10.0), 2_000L));
        assertFalse(tracker.record("c", OptionalDouble.of(20.0), 3_000L));
        assertTrue(tracker.record("d", OptionalDouble.of(20.0), 4_000L));
        assertFalse(tracker.record("e", OptionalDouble.of(10.0), 5_000L));
        assertFalse(tracker.record("f", OptionalDouble.of(20.0), 6_000L));
        assertTrue(tracker.record("g", OptionalDouble.of(30.0), 7_000L));
    }

    @Test
    public void oldEvidenceExpiresBeforeItCanFlag() {
        MiningAimTracker tracker = new MiningAimTracker(3, 4, 1_000L);
        assertFalse(tracker.record("a", OptionalDouble.of(10.0), 0L));
        assertFalse(tracker.record("b", OptionalDouble.of(20.0), 500L));
        assertFalse(tracker.record("c", OptionalDouble.of(30.0), 1_501L));
        assertFalse(tracker.record("d", OptionalDouble.of(40.0), 2_001L));
        assertTrue(tracker.record("e", OptionalDouble.of(50.0), 2_002L));
    }

    @Test
    public void occasionalPreciseHitsDoNotAccumulateIntoAViolation() {
        MiningAimTracker tracker = new MiningAimTracker(3, 4, 60_000L);
        for (int i = 0; i < 100; i++) {
            OptionalDouble pitch = i % 3 == 0 ? OptionalDouble.of(i) : OptionalDouble.empty();
            assertFalse(tracker.record("block-" + i, pitch, i * 100L));
        }
    }

    @Test
    public void aRowAtOnePitchDoesNotCountAsDiverseAim() {
        MiningAimTracker tracker = new MiningAimTracker(3, 4, 60_000L);
        for (int i = 0; i < 100; i++) {
            assertFalse(tracker.record("block-" + i, OptionalDouble.of(25.0), i * 100L));
        }
    }
}
