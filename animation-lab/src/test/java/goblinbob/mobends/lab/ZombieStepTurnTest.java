package goblinbob.mobends.lab;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the zombie and the zombie villager, checked by forward kinematics
 * ({@link PlantedFeetRig}): both animation sets (the lean, which lowers the whole model, and the
 * stumble), grown and baby. They walk by their clips and stand on their feet.
 */
public class ZombieStepTurnTest
{

    private static final float GROUND = PlantedFeetRig.GROUND;
    /** Zombies wander and chase at about 0.114 blocks a tick (attribute 0.23); babies at 0.257 (half again). */
    private static final double WALK = 0.114, BABY_WALK = 0.257;
    /** The animator's finishDrift: how far from its place a foot the walk left may stay standing. */
    private static final double SETTLE_RADIUS = 4;
    /** Entity ids that give each animation set (the model's animationSet, from the id). */
    private static final int LEAN = 3, STUMBLE = 1;

    @Test
    void settlesFromWhereverItStops() throws Exception
    {
        // Stopping two ticks apart across a whole stride (the clip's cycle is 9.4 of limb swing, which
        // vanilla advances by 4 times the speed, at most 1 a tick: about 21 ticks for a grown zombie),
        // the driver takes the feet over where the walk left them, on the ground, without sliding or
        // jumping them, and a foot left further than SETTLE_RADIUS from its place steps back.
        for (String kind : new String[] {"zombie", "baby zombie", "zombie villager"})
        {
            for (int set : new int[] {LEAN, STUMBLE})
            {
                for (int stopAt = 100; stopAt < 122; stopAt += 2)
                {
                    final int stop = stopAt;
                    PlantedFeetRig zombie = zombie(kind, set);
                    double speed = kind.startsWith("baby") ? BABY_WALK : WALK;
                    zombie.measureFrom = stop;
                    zombie.run(stop + 80, tick -> zombie.inputs.forwardSpeed = tick >= 40 && tick < stop ? speed : 0);

                    String at = " (" + kind + ", " + (set == LEAN ? "lean" : "stumble") + ") stopping at tick " + stop;
                    assertTrue(zombie.maxSlide < 0.02, "a planted foot slid " + zombie.maxSlide + " blocks" + at);
                    // Taken over where they are drawn: hardly faster than the walk moved them (a foot
                    // off the ground, or in it as the lean's are, also starts going to it).
                    assertTrue(zombie.maxFootJump < 1.75 * zombie.maxFootMoveBefore, "a foot jumped " + zombie.maxFootJump + " units in a frame, against "
                            + zombie.maxFootMoveBefore + " walking" + at);
                    for (int i = 0; i < 2; i++)
                    {
                        assertEquals(GROUND, zombie.soleY[i], 0.3, "foot " + i + " isn't on the ground" + at);
                        assertTrue(zombie.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + zombie.offRest[i] + " units off its place" + at);
                    }
                    assertTrue(zombie.steps <= 1, zombie.steps + " steps settling" + at);
                }
            }
        }
    }

    @Test
    void keepsItsHeightAsItStopsAndSetsOff() throws Exception
    {
        // Walking (the driver off) and standing (the driver on), the body is drawn at the same
        // height, and stopping or setting off doesn't jolt it.
        for (String kind : new String[] {"zombie", "baby zombie"})
        {
            for (int set : new int[] {LEAN, STUMBLE})
            {
                for (int stopAt = 100; stopAt < 122; stopAt += 5)
                {
                    final int stop = stopAt;
                    PlantedFeetRig zombie = zombie(kind, set);
                    double speed = kind.startsWith("baby") ? BABY_WALK : WALK;
                    zombie.run(stop + 80, tick -> zombie.inputs.forwardSpeed = tick >= 40 && (tick < stop || tick >= stop + 40) ? speed : 0);
                    String at = " (" + kind + ", " + (set == LEAN ? "lean" : "stumble") + ") stopping at tick " + stop;
                    // A baby's model units are half as large, so its hips move twice as many of them.
                    assertTrue(zombie.maxHipJump < 0.4, "the hips jumped " + zombie.maxHipJump + " units in a frame" + at);
                    // The leaning zombie is 3 lower, walking (its root) and standing (the driver's crouch).
                    double lowered = set == LEAN ? 3 : 0;
                    assertTrue(zombie.maxHipDrop < lowered + 1.6, "the hips went " + (zombie.maxHipDrop - lowered) + " units off their height walking" + at);
                }
            }
        }
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        for (String kind : new String[] {"zombie", "baby zombie"})
        {
            for (int set : new int[] {LEAN, STUMBLE})
            {
                PlantedFeetRig zombie = zombie(kind, set);
                final int turnAt = 60;
                // Vanilla turns the body 60° over 10 ticks, the head first.
                zombie.run(200, tick -> {
                    float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
                    zombie.inputs.bodyYaw = body;
                    zombie.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
                });
                String as = " (" + kind + ", " + (set == LEAN ? "lean" : "stumble") + ")";
                assertTrue(zombie.steps >= 2, "only " + zombie.steps + " steps" + as);
                assertTrue(zombie.maxSlide < 0.02, "a planted foot slid " + zombie.maxSlide + " blocks" + as);
                assertEquals(60, zombie.shownYaw, 2, "the turn didn't end squared up" + as);
                for (int i = 0; i < 2; i++)
                {
                    assertEquals(GROUND, zombie.soleY[i], 0.3, "foot " + i + " isn't on the ground" + as);
                }
            }
        }
    }

    /**
     * A zombie (no renderer scale), a baby one, or a zombie villager (ModelZombieVillager's legs are
     * 2 to the side, not 1.9), of an animation set.
     */
    private static PlantedFeetRig zombie(String kind, int set) throws Exception
    {
        boolean villager = kind.equals("zombie villager");
        float side = villager ? 2 : 1.9F;
        return new PlantedFeetRig(villager ? "bends/models/zombie_villager.json" : "bends/models/zombie.json",
                villager ? EntityZombieVillager::new : EntityZombie::new, kind.startsWith("baby"), set, 1,
                new PlantedFeetRig.Leg("rightLeg", "rightForeLeg", new float[] {0, 12, 0}, new float[] {0, 6, -2}, new float[] {-side, 6, 2}),
                new PlantedFeetRig.Leg("leftLeg", "leftForeLeg", new float[] {0, 12, 0}, new float[] {0, 6, -2}, new float[] {side, 6, 2}));
    }

}
