package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabWorlds;
import goblinbob.mobends.lab.sim.Scenario;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The golem's hands as it climbs onto a block (iron_golem.json, the climb): put on the top where
 * the rays find it, and held there, wherever the climb draws the golem and however it leans.
 */
class GolemClimbTest
{

    /**
     * Where the hands grab, as climb_ledge has it: the rays from the shoulders as the golem lifts
     * off (x 0.5 ∓ 0.6875) are aimed at three quarters of a block past the near edge (z 3) of the block's top.
     */
    private static final double SPOT_Z = 3.75;
    private static final double[] RIGHT_SPOT = {0.5 - 0.6875, 1, SPOT_Z};
    private static final double[] LEFT_SPOT = {0.5 + 0.6875, 1, SPOT_Z};

    /** Per frame of the climb: the climb's tick, then the right hand and the left in the world. */
    private static List<double[]> climb(Scenario scenario) throws Exception
    {
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)));
        DefinedEntityData<?> data = (DefinedEntityData<?>) session.data;
        List<double[]> frames = new ArrayList<>();
        double enteredAt = Double.NaN;
        for (int i = 0; i < scenario.frameCount(); i++)
        {
            session.step();
            if (!session.animator.getCurrentNodes().get(0).equals("climb"))
            {
                if (!Double.isNaN(enteredAt)) break;
                continue;
            }
            if (Double.isNaN(enteredAt)) enteredAt = session.clock.getTicks();
            double[] right = GolemHands.hand(data, true), left = GolemHands.hand(data, false);
            frames.add(new double[] {session.clock.getTicks() - enteredAt, right[0], right[1], right[2], left[0], left[1], left[2]});
        }
        return frames;
    }

    private static double[] right(double[] frame)
    {
        return new double[] {frame[1], frame[2], frame[3]};
    }

    private static double[] left(double[] frame)
    {
        return new double[] {frame[4], frame[5], frame[6]};
    }

    /** From when the hands have come down until the shoulders rise out of their reach (about tick 8.5, then they push). */
    private static boolean planted(double[] frame)
    {
        return frame[0] >= 4 && frame[0] <= 8;
    }

    @Test
    void theHandsHoldOnToTheTopOfTheLedge() throws Exception
    {
        List<double[]> frames = climb(Scenarios.byId("iron_golem/climb_ledge"));
        int held = 0;
        for (double[] frame : frames)
        {
            if (!planted(frame)) continue;
            held++;
            assertEquals(0, GolemHands.distance(right(frame), RIGHT_SPOT), 0.005, "the right hand at tick " + frame[0]);
            assertEquals(0, GolemHands.distance(left(frame), LEFT_SPOT), 0.005, "the left hand at tick " + frame[0]);
        }
        assertTrue(held >= 6, "the climb holds on for a while: " + held + " frames");
    }

    @Test
    void theHandsReachOutAlongTheRaysOntoTheTop() throws Exception
    {
        double[] last = null;
        int reaching = 0;
        for (double[] frame : climb(Scenarios.byId("iron_golem/climb_ledge")))
        {
            if (frame[0] < 2 || frame[0] >= 4) continue;
            assertTrue(frame[2] > 1.0, "above the top while reaching for it, at tick " + frame[0]);
            if (last != null)
            {
                // Forward (+Z) and down, from the shoulder's side of the ledge's edge.
                assertTrue(frame[3] > last[3] && frame[2] < last[2], "forward and down at tick " + frame[0]);
                reaching++;
            }
            last = frame;
        }
        assertTrue(reaching >= 2);
    }

    @Test
    void aLedgeOffToOneSideTurnsItTowardsTheLedge() throws Exception
    {
        // The ledge only on the left (x 0..2): square on, the right hand (x -0.2) would be over a
        // gap, but the ground rises towards the ledge's side, so the golem turns that way, and
        // the right hand comes over the ledge and grabs its top.
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_gap", Scenarios.FPS, 140, (tick, in) -> {
            if (tick >= 20 && tick < 110) Scripts.walk(in, Scripts.GOLEM_SPEED);
            if (tick == 42) in.jump = true;
        }, data -> LabWorlds.placeStone(data.getEntity().world, 0, 0, 3, 2, 0, 4));
        double[] grabbed = null;
        for (double[] frame : climb(scenario))
        {
            if (!planted(frame)) continue;
            double[] right = right(frame);
            if (grabbed == null) grabbed = right;
            assertTrue(right[0] >= 0 && right[0] <= 2, "the right hand over the ledge, at x " + right[0]);
            assertEquals(1, right[1], 0.005, "the right hand on the top at tick " + frame[0]);
            assertEquals(0, GolemHands.distance(right, grabbed), 0.005, "the right hand held at tick " + frame[0]);
            assertTrue(left(frame)[0] > right[0], "the left hand to its left at tick " + frame[0]);
        }
        assertTrue(grabbed != null, "it climbs");
    }

    @Test
    void aJumpShortOfTheLedgeStillClimbsOntoIt() throws Exception
    {
        // It jumps before reaching the face, 0.4 short of it (1.1 from it): the step it walked
        // towards is the climb's, so the hands go on its top where climb_ledge's do.
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_early", Scenarios.FPS, 140, (tick, in) -> {
            if (tick >= 20 && tick < 110) Scripts.walk(in, Scripts.GOLEM_SPEED);
            if (tick == 34) in.jump = true;
        }, data -> LabWorlds.placeStone(data.getEntity().world, -2, 0, 3, 2, 0, 4));
        int held = 0;
        for (double[] frame : climb(scenario))
        {
            if (!planted(frame)) continue;
            held++;
            assertEquals(0, GolemHands.distance(right(frame), RIGHT_SPOT), 0.005, "the right hand at tick " + frame[0]);
            assertEquals(0, GolemHands.distance(left(frame), LEFT_SPOT), 0.005, "the left hand at tick " + frame[0]);
        }
        assertTrue(held >= 6, "it holds on: " + held + " frames");
    }

    @Test
    void backingAwayFromTheLedgeItJumps() throws Exception
    {
        // It walks into the face, as climb_ledge does, then backs off and jumps.
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_backing_away", Scenarios.FPS, 100, (tick, in) -> {
            if (tick >= 20 && tick < 40) Scripts.walk(in, Scripts.GOLEM_SPEED);
            if (tick >= 42 && tick < 60) Scripts.walk(in, -Scripts.GOLEM_SPEED);
            if (tick == 46) in.jump = true;
        }, data -> LabWorlds.placeStone(data.getEntity().world, -2, 0, 3, 2, 0, 4));
        assertTrue(climb(scenario).isEmpty(), "no climb");
    }

    @Test
    void walkingAlongALedgeItJumps() throws Exception
    {
        // Facing +X, along the face of a ledge beside it (its near face at z 2, 0.8 from its side).
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_along", Scenarios.FPS, 80, (tick, in) -> {
            in.bodyYaw = -90;
            if (tick >= 20 && tick < 60) Scripts.walk(in, Scripts.GOLEM_SPEED);
            if (tick == 40) in.jump = true;
        }, data -> LabWorlds.placeStone(data.getEntity().world, -10, 0, 2, 10, 0, 3));
        assertTrue(climb(scenario).isEmpty(), "no climb");
    }

    @Test
    void aWallTooHighOrWithoutRoomOnItIsAJump() throws Exception
    {
        String[] cases = {"wall", "ceiling"};
        for (String which : cases)
        {
            Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_" + which, Scenarios.FPS, 100, (tick, in) -> {
                if (tick >= 20 && tick < 90) Scripts.walk(in, Scripts.GOLEM_SPEED);
                if (tick == 42) in.jump = true;
            }, data -> {
                LabWorlds.placeStone(data.getEntity().world, -2, 0, 3, 2, which.equals("wall") ? 1 : 0, 4);
                if (which.equals("ceiling")) LabWorlds.placeStone(data.getEntity().world, -2, 3, 3, 2, 3, 4);
            });
            assertTrue(climb(scenario).isEmpty(), "no climb at the " + which);
        }
    }

    @Test
    void walkingAwayFromTheLedgeItGivesUpEarly() throws Exception
    {
        // climb_ledge, but a few ticks into the climb it backs away from the ledge.
        double kept = climbLength(Scenarios.byId("iron_golem/climb_ledge"));
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_backing_off", Scenarios.FPS, 140, (tick, in) -> {
            if (tick >= 20 && tick < 110) Scripts.walk(in, tick >= 44 ? -Scripts.GOLEM_SPEED : Scripts.GOLEM_SPEED);
            if (tick == 42) in.jump = true;
        }, data -> LabWorlds.placeStone(data.getEntity().world, -2, 0, 3, 2, 0, 4));
        double given = climbLength(scenario);
        assertTrue(kept >= 15, "climb_ledge climbs on: " + kept + " ticks");
        assertTrue(given >= 3 && given < 10, "given up before it is drawn up onto the ledge: " + given + " ticks");
    }

    /** How many ticks the climb lasts. */
    private static double climbLength(Scenario scenario) throws Exception
    {
        List<double[]> frames = climb(scenario);
        assertFalse(frames.isEmpty(), "it climbs");
        return frames.get(frames.size() - 1)[0];
    }

    @Test
    void anOpenFieldJumpDoesntClimb() throws Exception
    {
        assertFalse(climb(Scenarios.byId("iron_golem/jump_stand_walk")).iterator().hasNext());
    }

}
