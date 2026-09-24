package me.chrommob.baritoneremover.checks.inter;

import com.github.retrooper.packetevents.util.Vector3d;
import me.chrommob.baritoneremover.data.PacketDatas;
import me.chrommob.baritoneremover.data.types.BlockTargetData;
import me.chrommob.baritoneremover.data.types.PositionData;
import me.chrommob.baritoneremover.data.types.RotationData;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.OptionalDouble;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MiningTargetingTest {
    private static final PositionData POSITION = new PositionData(new Vector3d(0.2, 64.0, 0.2));

    @Test
    public void fastManualMiningWithLatencyDoesNotBuildEitherPattern() {
        AtomicLong clock = new AtomicLong();
        PacketDatas packets = new PacketDatas(clock::get);
        packets.add(CheckType.POSITION, POSITION, null, false, false, false);
        MiningAimTracker approach = new MiningAimTracker(10, 12, 60_000L);
        MiningAimTracker hold = new MiningAimTracker(12, 15, 90_000L);

        for (int i = 0; i < 24; i++) {
            BlockTargetData target = target(i);
            RotationData expected = expected(target);
            boolean precise = i % 4 == 0;
            boolean held = i % 8 == 4;
            CanonicalTargeting.Result result = sample(packets, clock, target,
                    new RotationData(expected.pitch() + (precise || held ? 0.05f : 0.25f),
                            expected.yaw() + 0.7f),
                    new RotationData(expected.pitch() + (held ? 0.04f : 4.0f),
                            expected.yaw() + (held ? 0.5f : 5.0f)), 100L);
            assertNotNull(result);
            assertFalse(approach.record(target.key(), result.isPreciseMiningApproach()
                    ? OptionalDouble.of(result.expectedPitch()) : OptionalDouble.empty(), clock.get()));
            assertFalse(hold.record(target.key(), result.isPreciseMiningHold()
                    ? OptionalDouble.of(result.expectedPitch()) : OptionalDouble.empty(), clock.get()));
        }
    }

    @Test
    public void preciseSnapsAndHeldAimRemainDetectable() {
        AtomicLong clock = new AtomicLong();
        PacketDatas packets = new PacketDatas(clock::get);
        packets.add(CheckType.POSITION, POSITION, null, false, false, false);
        MiningAimTracker approach = new MiningAimTracker(10, 12, 60_000L);
        MiningAimTracker hold = new MiningAimTracker(12, 15, 90_000L);
        int approachBatches = 0;
        int holdBatches = 0;

        for (int i = 0; i < 24; i++) {
            BlockTargetData target = target(i);
            RotationData expected = expected(target);
            CanonicalTargeting.Result result;
            if (i < 12) {
                result = sample(packets, clock, target,
                        new RotationData(expected.pitch() + 0.05f, expected.yaw() + 0.7f),
                        new RotationData(expected.pitch() + 4.0f, expected.yaw() + 5.0f), 50L);
                assertTrue(result.isPreciseMiningApproach());
                assertFalse(result.isPreciseMiningHold());
            } else {
                result = sample(packets, clock, target,
                        new RotationData(expected.pitch() + 0.05f, expected.yaw() + 0.7f),
                        new RotationData(expected.pitch() + 0.04f, expected.yaw() + 0.5f), 100L);
                assertFalse(result.isPreciseMiningApproach());
                assertTrue(result.isPreciseMiningHold());
            }
            if (approach.record(target.key(), result.isPreciseMiningApproach()
                    ? OptionalDouble.of(result.expectedPitch()) : OptionalDouble.empty(), clock.get())) {
                approachBatches++;
            }
            if (hold.record(target.key(), result.isPreciseMiningHold()
                    ? OptionalDouble.of(result.expectedPitch()) : OptionalDouble.empty(), clock.get())) {
                holdBatches++;
            }
        }
        assertTrue("A should retain a bot-like detection path", approachBatches == 1);
        assertTrue("D should retain a bot-like detection path", holdBatches == 1);
    }

    @Test
    public void staleRotationAndMovementCannotBecomeEvidence() {
        AtomicLong clock = new AtomicLong();
        PacketDatas packets = new PacketDatas(clock::get);
        BlockTargetData target = target(0);
        RotationData expected = expected(target);
        packets.add(CheckType.POSITION, POSITION, null, false, false, false);
        packets.add(CheckType.ROTATION, null, new RotationData(expected.pitch() + 4.0f,
                expected.yaw() + 5.0f), false, false, false);
        clock.set(1_000L);
        packets.add(CheckType.ROTATION, null, expected, false, false, false);
        clock.set(1_050L);
        packets.add(CheckType.MINING, null, null, true, false, false, target);
        assertNull(MiningTargeting.evaluate(packets, packets.getLatest(), 250L));

        clock.set(2_000L);
        packets.add(CheckType.ROTATION, null, new RotationData(expected.pitch() + 4.0f,
                expected.yaw() + 5.0f), false, false, false);
        clock.set(2_050L);
        packets.add(CheckType.ROTATION, null, expected, false, false, false);
        clock.set(2_100L);
        packets.add(CheckType.POSITION, new PositionData(new Vector3d(0.5, 64.0, 0.2)), null,
                false, false, false);
        packets.add(CheckType.MINING, null, null, true, false, false, target);
        assertNull(MiningTargeting.evaluate(packets, packets.getLatest(), 250L));
    }

    @Test
    public void heldAimRequiresBothRotationsToMatchTheTarget() {
        AtomicLong clock = new AtomicLong();
        PacketDatas packets = new PacketDatas(clock::get);
        packets.add(CheckType.POSITION, POSITION, null, false, false, false);
        BlockTargetData target = target(0);
        RotationData expected = expected(target);
        CanonicalTargeting.Result result = sample(packets, clock, target,
                new RotationData(expected.pitch() + 0.05f, expected.yaw() + 0.7f),
                new RotationData(expected.pitch() + 0.25f, expected.yaw() + 0.5f), 50L);
        assertNotNull(result);
        assertFalse(result.isPreciseMiningHold());
    }

    private static CanonicalTargeting.Result sample(PacketDatas packets, AtomicLong clock, BlockTargetData target,
            RotationData current, RotationData previous, long actionDelay) {
        clock.addAndGet(1_000L);
        packets.add(CheckType.ROTATION, null, previous, false, false, false);
        clock.addAndGet(50L);
        packets.add(CheckType.ROTATION, null, current, false, false, false);
        clock.addAndGet(actionDelay);
        packets.add(CheckType.MINING, null, null, true, false, false, target);
        return MiningTargeting.evaluate(packets, packets.getLatest(), 250L);
    }

    private static BlockTargetData target(int index) {
        return BlockTargetData.mining(2 + index / 12, 63 + index % 4, index / 4 % 3,
                0, 1, 0, 1.62);
    }

    private static RotationData expected(BlockTargetData target) {
        double dx = POSITION.x() - target.x() - 0.5;
        double dy = POSITION.y() + target.eyeHeight() - target.y() - 0.5;
        double dz = POSITION.z() - target.z() - 0.5;
        float yaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
        float pitch = (float) Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        return new RotationData(pitch, yaw);
    }
}
