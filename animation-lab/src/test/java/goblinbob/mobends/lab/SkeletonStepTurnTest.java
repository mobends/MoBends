package goblinbob.mobends.lab;

import net.minecraft.world.entity.monster.Skeleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the skeleton, checked by forward kinematics ({@link PlantedFeetRig}):
 * it walks by its clips and stands on its feet.
 */
public class SkeletonStepTurnTest
{

    private static final float GROUND = PlantedFeetRig.GROUND;
    /** Skeletons wander and chase at about 0.124 blocks a tick (attribute 0.25). */
    private static final double WALK = 0.124;
    /** The animator's finishDrift: how far from its place a foot the walk left may stay standing. */
    private static final double SETTLE_RADIUS = 4;

    @Test
    void settlesFromWhereverItStops() throws Exception
    {
        // Stopping two ticks apart across a whole stride, the driver takes the feet over where the
        // walk left them, on the ground, without sliding or jumping them, and a foot left further
        // than SETTLE_RADIUS from its place steps back.
        for (int stopAt = 100; stopAt < 122; stopAt += 2)
        {
            final int stop = stopAt;
            PlantedFeetRig skeleton = skeleton();
            skeleton.measureFrom = stop;
            skeleton.run(stop + 80, tick -> skeleton.inputs.forwardSpeed = tick >= 40 && tick < stop ? WALK : 0);

            String at = " stopping at tick " + stop;
            assertTrue(skeleton.maxSlide < 0.02, "a planted foot slid " + skeleton.maxSlide + " blocks" + at);
            assertTrue(skeleton.maxFootJump < 1.75 * skeleton.maxFootMoveBefore, "a foot jumped " + skeleton.maxFootJump + " units in a frame, against "
                    + skeleton.maxFootMoveBefore + " walking" + at);
            for (int i = 0; i < 2; i++)
            {
                assertEquals(GROUND, skeleton.soleY[i], 0.3, "foot " + i + " isn't on the ground" + at);
                assertTrue(skeleton.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + skeleton.offRest[i] + " units off its place" + at);
            }
            assertTrue(skeleton.steps <= 1, skeleton.steps + " steps settling" + at);
        }
    }

    @Test
    void keepsItsHeightAsItStopsAndSetsOff() throws Exception
    {
        // Walking (the driver off) and standing (the driver on), the body is drawn at the same
        // height, and stopping or setting off doesn't jolt it.
        for (int stopAt = 100; stopAt < 122; stopAt += 5)
        {
            final int stop = stopAt;
            PlantedFeetRig skeleton = skeleton();
            skeleton.run(stop + 80, tick -> skeleton.inputs.forwardSpeed = tick >= 40 && (tick < stop || tick >= stop + 40) ? WALK : 0);
            String at = " stopping at tick " + stop;
            assertTrue(skeleton.maxHipJump < 0.4, "the hips jumped " + skeleton.maxHipJump + " units in a frame" + at);
            assertTrue(skeleton.maxHipDrop < 1.6, "the hips went " + skeleton.maxHipDrop + " units off their height walking" + at);
        }
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        PlantedFeetRig skeleton = skeleton();
        final int turnAt = 60;
        // Vanilla turns the body 60° over 10 ticks, the head first.
        skeleton.run(200, tick -> {
            float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
            skeleton.inputs.bodyYaw = body;
            skeleton.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
        });
        assertTrue(skeleton.steps >= 2, "only " + skeleton.steps + " steps");
        assertTrue(skeleton.maxSlide < 0.02, "a planted foot slid " + skeleton.maxSlide + " blocks");
        assertEquals(60, skeleton.shownYaw, 2, "the turn didn't end squared up");
        for (int i = 0; i < 2; i++)
        {
            assertEquals(GROUND, skeleton.soleY[i], 0.3, "foot " + i + " isn't on the ground");
        }
    }

    /** A skeleton: ModelSkeleton's legs are 2 wide and turn 2 to the side of the middle. */
    private static PlantedFeetRig skeleton() throws Exception
    {
        return new PlantedFeetRig("bends/models/skeleton.json", Skeleton::new, false, 0, 1,
                new PlantedFeetRig.Leg("rightLeg", "rightForeLeg", new float[] {-2, 12, 0}, new float[] {0, 6, -1}, new float[] {0, 6, 1}),
                new PlantedFeetRig.Leg("leftLeg", "leftForeLeg", new float[] {2, 12, 0}, new float[] {0, 6, -1}, new float[] {0, 6, 1}));
    }

}
