package goblinbob.mobends.lab;

import net.minecraft.client.entity.AbstractClientPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the player, checked by forward kinematics ({@link PlantedFeetRig}),
 * drawn at RenderPlayer's scale: it walks and runs by its clips and stands on its feet, except
 * where something else poses its legs.
 */
public class PlayerStepTurnTest
{

    private static final float GROUND = PlantedFeetRig.GROUND;
    /** A player walks at about 0.216 blocks a tick and sprints at 0.281. */
    private static final double WALK = 0.216, SPRINT = 0.281;
    /** The animator's finishDrift: how far from its place a foot the walk left may stay standing. */
    private static final double SETTLE_RADIUS = 4;

    @Test
    void settlesFromWhereverItStops() throws Exception
    {
        // Stopping a tick apart across a whole stride (about 11 ticks walking), from a walk and from
        // a sprint, the driver takes the feet over where the legs are drawn, on the ground, without
        // sliding or jumping them, and a foot left further than SETTLE_RADIUS from its place steps
        // back.
        for (boolean sprint : new boolean[] {false, true})
        {
            for (int stopAt = 100; stopAt < 112; stopAt++)
            {
                final int stop = stopAt;
                PlantedFeetRig player = player();
                player.measureFrom = stop;
                player.run(stop + 80, tick -> {
                    boolean moving = tick >= 40 && tick < stop;
                    player.inputs.forwardSpeed = moving ? (sprint ? SPRINT : WALK) : 0;
                    player.inputs.sprinting = moving && sprint;
                });
                String at = (sprint ? " (sprinting)" : "") + " stopping at tick " + stop;
                assertTrue(player.maxSlide < 0.02, "a planted foot slid " + player.maxSlide + " blocks" + at);
                // Taken over where they are drawn: hardly faster than the walk moved them.
                assertTrue(player.maxFootJump < 1.75 * player.maxFootMoveBefore, "a foot jumped " + player.maxFootJump + " units in a frame, against "
                        + player.maxFootMoveBefore + " walking" + at);
                for (int i = 0; i < 2; i++)
                {
                    assertEquals(GROUND, player.soleY[i], 0.3, "foot " + i + " isn't on the ground" + at);
                    assertTrue(player.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + player.offRest[i] + " units off its place" + at);
                }
                assertTrue(player.steps <= 2, player.steps + " steps settling" + at);
            }
        }
    }

    @Test
    void keepsItsHeightAsItStopsAndSetsOff() throws Exception
    {
        // Walking (the driver off) and standing (the driver on), the body is drawn at the same
        // height, and stopping or setting off doesn't jolt it.
        for (int stopAt = 100; stopAt < 112; stopAt += 3)
        {
            final int stop = stopAt;
            PlantedFeetRig player = player();
            player.run(stop + 80, tick -> player.inputs.forwardSpeed = tick >= 40 && (tick < stop || tick >= stop + 40) ? WALK : 0);
            String at = " stopping at tick " + stop;
            // The walk's own bob moves the hips up to 0.26 a frame as it sets off, the driver's crouch
            // for reach easing out with it.
            assertTrue(player.maxHipJump < 0.4, "the hips jumped " + player.maxHipJump + " units in a frame" + at);
            // Standing with the feet apart, they go down as far as the feet need to reach (at most
            // the animator's walkCrouch, 1), and the steps' landings dip them a little.
            assertTrue(player.maxHipDrop < 1.6, "the hips went " + player.maxHipDrop + " units off their height walking" + at);
        }
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        PlantedFeetRig player = player();
        final int turnAt = 60;
        player.run(200, tick -> {
            float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
            player.inputs.bodyYaw = body;
            player.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
        });
        assertTrue(player.steps >= 2, "only " + player.steps + " steps");
        assertTrue(player.maxSlide < 0.02, "a planted foot slid " + player.maxSlide + " blocks");
        // It squares up only past 25° after a step (finishThreshold), so it may end a little short,
        // the head still looking where the player looks.
        assertEquals(60, player.shownYaw, 10, "the turn ended far from where the player faces");
        for (int i = 0; i < 2; i++)
        {
            assertEquals(GROUND, player.soleY[i], 0.3, "foot " + i + " isn't on the ground");
        }
    }

    @Test
    void turnsQuickly() throws Exception
    {
        // Vanilla turns the body at once; on its feet, it catches up within a few steps.
        int[][] limits = {{60, 15}, {90, 17}, {150, 23}};
        for (int[] limit : limits)
        {
            int degrees = limit[0];
            PlantedFeetRig player = player();
            int[] within = {-1};
            player.run(140, tick -> {
                player.inputs.bodyYaw = tick < 60 ? 0 : degrees;
                if (tick > 60 && within[0] < 0 && Math.abs(player.shownYaw - degrees) < 5)
                {
                    within[0] = tick - 60;
                }
            });
            assertTrue(within[0] > 0 && within[0] <= limit[1], "turning " + degrees + "°, it was within 5° after " + within[0] + " ticks");
            assertTrue(player.maxSlide < 0.02, "turning " + degrees + "°, a planted foot slid " + player.maxSlide + " blocks");
        }
    }

    @Test
    void turnsInFewLargeSteps() throws Exception
    {
        // A quarter turn at once in a step of each foot; turning slowly (180° over 60 ticks, as a
        // player drags the view round), a step every 6 ticks or so, not a shuffle.
        PlantedFeetRig quarter = player();
        quarter.run(140, tick -> quarter.inputs.bodyYaw = tick < 60 ? 0 : 90);
        assertTrue(quarter.steps <= 2, "turning 90°, it took " + quarter.steps + " steps");
        PlantedFeetRig slow = player();
        slow.run(200, tick -> slow.inputs.bodyYaw = tick < 60 ? 0 : Math.min(180, (tick - 60) * 3));
        assertTrue(slow.steps <= 12, "turning 180° slowly, it took " + slow.steps + " steps");
        assertTrue(slow.maxSlide < 0.02, "turning slowly, a planted foot slid " + slow.maxSlide + " blocks");
    }

    @Test
    void leavesItsLegsAloneWhileSneakingOrFighting() throws Exception
    {
        // Vanilla turns the standing body 60° at once: on its feet, the shown body waits for them
        // to step; with the driver off, it turns with vanilla's.
        for (String what : new String[] {"standing", "sneaking", "after an attack"})
        {
            PlantedFeetRig player = player();
            double[] shownAfter = {Double.NaN};
            player.run(70, tick -> {
                player.inputs.bodyYaw = tick < 60 ? 0 : 60;
                player.inputs.sneaking = what.equals("sneaking");
                player.inputs.attack = what.equals("after an attack") && tick == 30;
                if (tick == 62)
                {
                    shownAfter[0] = player.shownYaw;
                }
            });
            if (what.equals("standing"))
            {
                assertTrue(shownAfter[0] < 10, "standing, the body turned " + shownAfter[0] + "° before its feet stepped");
            }
            else
            {
                // (After an attack the fist guard turns the whole model a further 20°.)
                assertTrue(shownAfter[0] > 55, what + ", the body was held at " + shownAfter[0] + "° by its feet");
            }
        }
    }

    @Test
    void standsWideInTheSwordStance() throws Exception
    {
        PlantedFeetRig player = player();
        player.measureFrom = 41;
        // Standing, then attacking with a sword at tick 30: the stance from tick 40 to 90.
        double[] spread = {0, 0};
        player.onFrame = time -> {
            if (Math.abs(time - 25) < 0.2)
            {
                spread[0] = player.feetApart();
            }
            if (Math.abs(time - 80) < 0.2)
            {
                spread[1] = player.feetApart();
            }
        };
        player.run(160, tick -> {
            player.inputs.mainHand = new net.minecraft.item.ItemStack(net.minecraft.init.Items.IRON_SWORD);
            player.inputs.attack = tick == 30;
        });
        // In the stance the feet stand more than twice as far apart, planted as the stance lowers and
        // turns the whole model side-on.
        assertTrue(spread[1] > 2 * spread[0], "in the stance, the feet were " + spread[1] + " apart, against " + spread[0] + " standing");
        assertTrue(player.maxSlide < 0.02, "a planted foot slid " + player.maxSlide + " blocks");
        for (int i = 0; i < 2; i++)
        {
            assertEquals(GROUND, player.soleY[i], 0.3, "foot " + i + " isn't on the ground");
            // After it, back within reach of their places.
            assertTrue(player.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + player.offRest[i] + " units off its place");
        }
    }

    private static PlantedFeetRig player() throws Exception
    {
        return new PlantedFeetRig("bends/models/player.json", AbstractClientPlayer::new, false, 0, 0.9375,
                new PlantedFeetRig.Leg("rightLeg", "rightForeLeg", new float[] {0, 12, 0}, new float[] {0, 6, -2}, new float[] {-1.9F, 6, 2}),
                new PlantedFeetRig.Leg("leftLeg", "leftForeLeg", new float[] {0, 12, 0}, new float[] {0, 6, -2}, new float[] {1.9F, 6, 2}));
    }

}
