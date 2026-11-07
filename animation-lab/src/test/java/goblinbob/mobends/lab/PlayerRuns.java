package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;

import java.util.ArrayList;
import java.util.List;

/** Runs a scripted player through its animator, for tests that compare two runs frame by frame. */
final class PlayerRuns
{

    static final int FPS = 20;
    /** The player animator's action layer: the slashes, punches, tool swings. */
    static final int ACTION_LAYER = 4;

    private PlayerRuns()
    {
    }

    /** Each frame's poses and the action layer's node. */
    static final class Run
    {
        final List<FramePose> frames = new ArrayList<>();
        final List<String> actionNodes = new ArrayList<>();

        /** The most a bone turns from where it is in {@code other}, over all frames, in degrees. */
        double worstAngle(Run other, String bone)
        {
            double worst = 0;
            for (int f = 0; f < frames.size(); f++)
            {
                worst = Math.max(worst, angleBetween(frames.get(f).bones.get(bone).r, other.frames.get(f).bones.get(bone).r));
            }
            return worst;
        }
    }

    static Run run(int ticks, Scenario.InputScript script) throws Exception
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

    /** The angle between two orientations (quaternions as x, y, z, w), in degrees. */
    static double angleBetween(float[] a, float[] b)
    {
        double dot = Math.abs(a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

}
