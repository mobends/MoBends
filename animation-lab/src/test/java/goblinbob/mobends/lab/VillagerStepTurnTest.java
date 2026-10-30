package goblinbob.mobends.lab;

import goblinbob.mobends.core.math.Quaternion;
import net.minecraft.entity.passive.EntityVillager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the villager, grown and baby, checked by forward kinematics
 * ({@link PlantedFeetRig}) as {@link StepTurnTest} checks the golem's, drawn at RenderVillager's
 * scale.
 */
public class VillagerStepTurnTest
{

    private static final float GROUND = PlantedFeetRig.GROUND;
    /** Villagers wander at about 0.19 blocks a tick (attribute 0.5, EntityAIWanderAvoidWater at 0.6). */
    private static final double WANDER = 0.19;
    /** The animator's finishDrift: how far from its place a foot the walk left may stay standing. */
    private static final double SETTLE_RADIUS = 4;

    @Test
    void settlesFromWhereverItStops() throws Exception
    {
        // It walks by a clip, its feet sliding; stopping at any point of the stride, the driver
        // takes the feet over where the walk left them, without sliding them, and a foot left
        // further than SETTLE_RADIUS from its place steps back under the body. Grown and baby, stopping a tick apart across a whole stride (the clip's
        // cycle is 9.4 of limb swing, which vanilla advances by 4 times the speed, at most 1 a tick:
        // about 12 ticks at a wander).
        for (boolean child : new boolean[] {false, true})
        {
            int stayed = 0;
            for (int stopAt = 100; stopAt < 113; stopAt++)
            {
                final int stop = stopAt;
                PlantedFeetRig villager = villager(child);
                villager.measureFrom = stop;
                villager.run(stop + 80, tick -> villager.inputs.forwardSpeed = tick >= 40 && tick < stop ? WANDER : 0);

                String at = (child ? " (baby)" : "") + " stopping at tick " + stop;
                assertTrue(villager.maxSlide < 0.02, "a planted foot slid " + villager.maxSlide + " blocks" + at);
                // Taken over where they are: hardly faster than the walk moved them (a foot in the air
                // also starts coming down). Placed at rest, they jump up to four times as far.
                assertTrue(villager.maxFootJump < 1.75 * villager.maxFootMoveBefore, "a foot jumped " + villager.maxFootJump + " units in a frame, against "
                        + villager.maxFootMoveBefore + " walking" + at);
                for (int i = 0; i < 2; i++)
                {
                    assertEquals(GROUND, villager.soleY[i], 0.3, "foot " + i + " isn't on the ground" + at);
                    assertTrue(villager.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + villager.offRest[i] + " units off its place" + at);
                }
                // A foot the walk left out of place steps back once, not shuffling after the walk's
                // lingering velocity; one nearer stays where it is.
                assertTrue(villager.steps <= 1, villager.steps + " steps settling" + at);
                if (villager.steps == 0)
                {
                    stayed++;
                }
            }
            // Stopping anywhere in the stride, the feet are often left where they are.
            assertTrue(stayed >= 3, "the feet stayed where the walk left them after only " + stayed + " of 13 stops" + (child ? " (baby)" : ""));
        }
    }

    @Test
    void keepsItsTorsoAndArmsWithinTheirTwist() throws Exception
    {
        // The upper body only twists a little with the steps (12° at most, eased in on springs),
        // walking and turning, however long it goes on: nothing accumulates from frame to frame.
        PlantedFeetRig villager = villager(false);
        double[] most = new double[2];
        Quaternion[] armsAtRest = new Quaternion[1];
        villager.onFrame = time -> {
            most[0] = Math.max(most[0], PlantedFeetRig.angle(villager.data.getPart("body").rotation.getSmooth()));
            Quaternion arms = villager.data.getPart("arms").rotation.getSmooth();
            if (armsAtRest[0] == null)
            {
                armsAtRest[0] = new Quaternion(arms.x, arms.y, arms.z, arms.w);
            }
            most[1] = Math.max(most[1], PlantedFeetRig.angleBetween(armsAtRest[0], arms));
        };
        villager.run(400, tick -> {
            villager.inputs.forwardSpeed = tick >= 40 && tick < 300 ? WANDER : 0;
            villager.inputs.bodyYaw = tick < 100 ? 0 : tick < 200 ? Math.min(120, (tick - 100) * 3F) : Math.max(-60, 120 - (tick - 200) * 3F);
        });
        assertTrue(most[0] < 13, "the torso turned " + most[0] + "°");
        assertTrue(most[1] < 20, "the arms turned " + most[1] + "° off their rest");
    }

    @Test
    void keepsItsHeightAsItStopsAndSetsOff() throws Exception
    {
        // Walking (the driver off) and standing (the driver on), the body is drawn at the same
        // height, and stopping or setting off doesn't jolt it: grown and baby, stopping at
        // different points of the stride, standing a while, then setting off again.
        for (boolean child : new boolean[] {false, true})
        {
            for (int stopAt = 100; stopAt < 113; stopAt += 3)
            {
                final int stop = stopAt;
                PlantedFeetRig villager = villager(child);
                villager.run(stop + 80, tick -> villager.inputs.forwardSpeed = tick >= 40 && (tick < stop || tick >= stop + 40) ? WANDER : 0);
                String at = (child ? " (baby)" : "") + " stopping at tick " + stop;
                assertTrue(villager.maxHipJump < 0.3, "the hips jumped " + villager.maxHipJump + " units in a frame" + at);
                // Standing with the feet apart, they go down as far as the feet need to reach (at most
                // the animator's walkCrouch, 1), and the steps' landings dip them a little.
                assertTrue(villager.maxHipDrop < 1.6, "the hips went " + villager.maxHipDrop + " units off their height walking" + at);
            }
        }
    }

    @Test
    void wobblesItsNoseWithEachStep() throws Exception
    {
        // Walking, the nose swings out at every footfall (twice a stride: about 12 ticks a stride at
        // a wander), a few degrees, and hardly back into the face.
        PlantedFeetRig walker = villager(false);
        List<double[]> walkerNose = recordNose(walker);
        walker.run(260, tick -> walker.inputs.forwardSpeed = tick >= 40 && tick < 160 ? WANDER : 0);
        int flicks = 0;
        double out = 0, in = 0;
        for (int f = 1; f + 1 < walkerNose.size(); f++)
        {
            double[] before = walkerNose.get(f - 1), now = walkerNose.get(f), after = walkerNose.get(f + 1);
            if (now[0] >= 60 && now[0] < 160)
            {
                out = Math.max(out, -now[1]);
                in = Math.max(in, now[1]);
                // A swing out peaking: the most outward (negative) frame round it, and well out.
                if (now[1] < before[1] && now[1] <= after[1] && now[1] < -3)
                {
                    flicks++;
                }
            }
        }
        assertTrue(flicks >= 100 / 12.4 * 2 * 0.8, "the nose swung out " + flicks + " times in 100 ticks of walking");
        assertTrue(out > 4 && out < 10, "the nose swung out " + out + "°");
        assertTrue(in < 2.5, "the nose swung " + in + "° into the face");
        // Standing still a while, it hangs at rest.
        double last = walkerNose.get(walkerNose.size() - 1)[1];
        assertEquals(0, last, 1, "the nose still hangs at " + last + "° after standing");

        // Turning on the spot, the steps the driver takes wobble it too.
        PlantedFeetRig turner = villager(false);
        List<double[]> turnerNose = recordNose(turner);
        turner.run(160, tick -> {
            float body = tick < 60 ? 0 : Math.min(60, (tick - 60) * 6);
            turner.inputs.bodyYaw = body;
            turner.inputs.headYaw = (tick < 50 ? 0 : 60) - body;
        });
        double turning = 0;
        for (double[] frame : turnerNose)
        {
            if (frame[0] >= 60)
            {
                turning = Math.max(turning, -frame[1]);
            }
        }
        assertTrue(turning > 2 && turning < 8, "turning, the nose swung out " + turning + "°");
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        for (boolean child : new boolean[] {false, true})
        {
            PlantedFeetRig villager = villager(child);
            final int turnAt = 60;
            // Vanilla turns the body 60° over 10 ticks, the head first.
            villager.run(200, tick -> {
                float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
                villager.inputs.bodyYaw = body;
                villager.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
            });
            String as = child ? " (baby)" : "";
            assertTrue(villager.steps >= 2, "only " + villager.steps + " steps" + as);
            assertTrue(villager.maxSlide < 0.02, "a planted foot slid " + villager.maxSlide + " blocks" + as);
            assertEquals(60, villager.shownYaw, 1, "the turn didn't end squared up" + as);
        }
    }

    /** A villager, grown or baby: RenderVillager draws it at 0.9375. */
    private static PlantedFeetRig villager(boolean child) throws Exception
    {
        return new PlantedFeetRig("bends/models/villager.json", EntityVillager::new, child, 0, 0.9375,
                new PlantedFeetRig.Leg("rightLeg", "foreRightLeg", new float[] {-2, 12, 0}, new float[] {0, 6, -2}, new float[] {0, 6, 2}),
                new PlantedFeetRig.Leg("leftLeg", "foreLeftLeg", new float[] {2, 12, 0}, new float[] {0, 6, -2}, new float[] {0, 6, 2}));
    }

    /** The nose's swing about X every frame (degrees, out is negative), with the time in ticks. */
    private static List<double[]> recordNose(PlantedFeetRig villager)
    {
        List<double[]> nose = new ArrayList<>();
        villager.onFrame = time -> {
            Quaternion q = villager.data.getPart("nose").rotation.getSmooth();
            nose.add(new double[] {time, Math.toDegrees(2 * Math.atan2(q.x, q.w))});
        };
        return nose;
    }

}
