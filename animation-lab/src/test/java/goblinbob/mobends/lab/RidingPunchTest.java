package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Punching while riding (#238). */
class RidingPunchTest
{

    private static final int FPS = 20;
    private static final int ACTION_LAYER = 4;

    /** Each frame's poses and the action layer's node. */
    private static final class Run
    {
        final List<FramePose> frames = new ArrayList<>();
        final List<String> actionNodes = new ArrayList<>();
    }

    private static Run run(int ticks, Scenario.InputScript script) throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.PLAYER, "issue", FPS, ticks, script);
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)));
        Run run = new Run();
        for (int f = 0; f < scenario.frameCount(); f++)
        {
            run.frames.add(session.step());
            run.actionNodes.add(session.animator.getCurrentNodes().get(ACTION_LAYER));
        }
        return run;
    }

    private static double angleBetween(float[] a, float[] b)
    {
        double dot = Math.abs(a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

    // --- #238: punching from the saddle leaves the legs and the root as riding has them ---------------------

    @Test
    void punchingWhileRidingLeavesTheLegsSeated() throws Exception
    {
        Scenario.InputScript riding = (tick, in) -> {
            in.noGravity = true;
            in.riding = true;
        };
        Run still = run(80, riding);
        Run punching = run(80, (tick, in) -> {
            riding.fill(tick, in);
            if (tick == 20 || tick == 32) in.attack = true;
        });
        assertTrue(punching.actionNodes.contains("punch_right") && punching.actionNodes.contains("fist_guard"));
        double worstLeg = 0, worstRoot = 0;
        String worst = "";
        for (int f = 0; f < still.frames.size(); f++)
        {
            for (String bone : new String[] {"rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg", "renderRotation"})
            {
                double angle = angleBetween(still.frames.get(f).bones.get(bone).r, punching.frames.get(f).bones.get(bone).r);
                if (angle > worstLeg)
                {
                    worstLeg = angle;
                    worst = "frame " + f + " " + bone;
                }
            }
            worstRoot = Math.max(worstRoot, Math.abs(still.frames.get(f).vectors.get("globalOffset").v[1]
                    - punching.frames.get(f).vectors.get("globalOffset").v[1]));
        }
        assertTrue(worstLeg < 0.5, "the punches turned the legs by " + worstLeg + " degrees (" + worst + ")");
        assertTrue(worstRoot < 0.01, "the punches lowered the rider by " + worstRoot);
    }
}
