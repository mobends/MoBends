package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabWorlds;
import goblinbob.mobends.lab.sim.Scenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The golem climbing onto a wide ledge facing every way: the four cardinal directions, and at
 * angles off them, walking into the ledge diagonally (it slides along the face as it climbs). The
 * climb squares the body up to the ledge, so the rays from its shoulders find the top three quarters of a block
 * past the near edge, ahead of each shoulder, and the hands hold on there while the golem is held
 * where it lifted off.
 */
class GolemClimbDirectionsTest
{

    @ParameterizedTest(name = "yaw {0}")
    @ValueSource(ints = {0, 90, 180, -90, 15, 30, 40, -30, 120, 210})
    void theHandsGrabTheTopAheadOfTheShoulders(int yaw) throws Exception
    {
        double rad = Math.toRadians(yaw);
        double faceX = -Math.sin(rad), faceZ = Math.cos(rad);
        // The axis nearest the way it faces, which it climbs along, and the left of it.
        final int fx = Math.abs(faceX) > Math.abs(faceZ) ? (faceX > 0 ? 1 : -1) : 0;
        final int fz = fx == 0 ? (faceZ > 0 ? 1 : -1) : 0;
        final int lx = fz, lz = -fx;
        final int sign = fx + fz;
        // The ledge: cells 3..4 ahead of the start's (going the negative way, -4..-3), 17 wide; its near face in the world.
        final int near = 3, far = 4;
        final double face = sign > 0 ? near : -near + 1;
        // It jumps a few ticks after it walks into the face, as climb_ledge does.
        final int jumpAt = 20 + (int) Math.ceil(1.8 / (Scripts.GOLEM_SPEED * (faceX * fx + faceZ * fz))) + 4;
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_yaw_" + yaw, Scenarios.FPS, 160, (tick, in) -> {
            in.bodyYaw = yaw;
            if (tick >= 20 && tick < 130) Scripts.walk(in, Scripts.GOLEM_SPEED);
            if (tick == jumpAt) in.jump = true;
        }, data -> {
            int a0 = sign > 0 ? near : -far, a1 = sign > 0 ? far : -near;
            if (fx != 0) LabWorlds.placeStone(data.getEntity().world, a0, 0, -8, a1, 0, 8);
            else LabWorlds.placeStone(data.getEntity().world, -8, 0, a0, 8, 0, a1);
        });

        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)));
        DefinedEntityData<?> data = (DefinedEntityData<?>) session.data;
        double enteredAt = Double.NaN, startSide = Double.NaN;
        double[][] first = new double[2][];
        int planted = 0;
        for (int i = 0; i < scenario.frameCount(); i++)
        {
            session.step();
            if (!session.animator.getCurrentNodes().get(0).equals("climb"))
            {
                if (!Double.isNaN(enteredAt)) break;
                continue;
            }
            if (Double.isNaN(enteredAt))
            {
                enteredAt = session.clock.getTicks();
                startSide = data.getVariable("entityWorldX") * lx + data.getVariable("entityWorldZ") * lz;
            }
            double t = session.clock.getTicks() - enteredAt;
            // From when the hands have come down until the shoulders rise out of their reach (about tick 8.5, then they push).
            if (t < 4 || t > 8) continue;
            planted++;
            // The body as drawn: its left from shoulder to shoulder, its forward across that.
            double[] rs = GolemHands.toWorld(data, new double[] {-11, -7, 0}), ls = GolemHands.toWorld(data, new double[] {11, -7, 0});
            double across = Math.hypot(ls[0] - rs[0], ls[2] - rs[2]);
            double leftX = (ls[0] - rs[0]) / across, leftZ = (ls[2] - rs[2]) / across;
            // Squared up to the ledge: its forward is the way it climbs.
            assertEquals(1, -leftZ * fx + leftX * fz, 1e-3, "facing the ledge at tick " + t);
            for (int h = 0; h < 2; h++)
            {
                double[] hand = GolemHands.hand(data, h == 0);
                if (first[h] == null)
                {
                    first[h] = hand;
                    assertEquals(1, hand[1], 0.005, "on the top");
                    assertEquals(0.75, hand[0] * fx + hand[2] * fz - face * sign, 0.01, "three quarters of a block past the edge");
                    assertEquals(h == 0 ? -0.6875 : 0.6875, hand[0] * lx + hand[2] * lz - startSide, 0.01, "ahead of its shoulder");
                }
                assertEquals(0, GolemHands.distance(hand, first[h]), 0.005, "holds still at tick " + t);
                double[] shoulder = h == 0 ? rs : ls;
                double outward = ((hand[0] - shoulder[0]) * leftX + (hand[2] - shoulder[2]) * leftZ) * (h == 0 ? -1 : 1);
                assertEquals(0, outward, 0.01, "straight ahead of its shoulder at tick " + t);
            }
        }
        assertTrue(!Double.isNaN(enteredAt), "it climbs");
        assertTrue(planted >= 6, "it holds on: " + planted + " frames");
    }

}
