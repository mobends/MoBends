package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabWorlds;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.util.List;

import static goblinbob.mobends.lab.scenarios.Scripts.GOLEM_SPEED;
import static goblinbob.mobends.lab.scenarios.Scripts.between;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The iron golem climbs onto a block the same way whichever way the block is from it (from walking on). */
class ClimbDirectionTest
{

    /** The golem (starting at 0.5, 0, 0.5) walks facing {@code yaw} into a block 2.5 blocks ahead, and jumps onto it. */
    private static List<FramePose> climb(float yaw, int x0, int z0, int x1, int z1) throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.IRON_GOLEM, "climb_" + yaw, Scenarios.FPS, 80, (tick, in) -> {
            in.bodyYaw = yaw;
            if (between(tick, 20, 75)) in.forwardSpeed = GOLEM_SPEED;
            if (tick == 42) in.jump = true;
        }, data -> {
            // Facing that way from the start, so it doesn't turn on its feet first.
            net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) data.getEntity();
            entity.renderYawOffset = entity.prevRenderYawOffset = yaw;
            entity.rotationYaw = entity.prevRotationYaw = entity.rotationYawHead = entity.prevRotationYawHead = yaw;
            LabWorlds.placeStone(entity.world, x0, 0, z0, x1, 0, z1);
        });
        return new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.IRON_GOLEM))).run().frames;
    }

    private static double angle(float[] a, float[] b)
    {
        double dot = Math.abs(a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

    @Test
    void theClimbIsTheSameFromEverySide() throws Exception
    {
        List<FramePose> south = climb(0, -2, 3, 2, 4);
        float highest = 0;
        for (FramePose frame : south) highest = Math.max(highest, Math.abs(frame.vectors.get("globalOffset").v[1]));
        assertTrue(highest > 10, "the climb holds the golem down while it rises (" + highest + ")");

        Object[][] sides = {{"north", climb(180, -2, -4, 2, -3)}, {"east", climb(-90, 3, -2, 4, 2)}, {"west", climb(90, -4, -2, -3, 2)}};
        for (Object[] side : sides)
        {
            @SuppressWarnings("unchecked")
            List<FramePose> frames = (List<FramePose>) side[1];
            for (int i = 0; i < south.size(); i++)
            {
                FramePose a = south.get(i), b = frames.get(i);
                if (a.tick < 30) continue;
                for (int axis = 0; axis < 3; axis++)
                {
                    assertEquals(a.vectors.get("globalOffset").v[axis], b.vectors.get("globalOffset").v[axis], 0.05,
                            side[0] + ": the whole model's offset, axis " + axis + ", at tick " + a.tick);
                }
                for (String bone : new String[] {"centerRotation", "rightArm", "rightForeArm", "rightLeg", "leftLeg"})
                {
                    assertTrue(angle(a.bones.get(bone).r, b.bones.get(bone).r) < 0.5, side[0] + ": " + bone + " at tick " + a.tick);
                }
            }
        }
    }

}
