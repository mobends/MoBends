package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/** Drivers registered through the API: inputs, bones, states, outputs, declared state and the pose writer. */
public class DriverApiTest
{

    public static class RaiseTemplate extends DriverItemTemplate
    {
        public String bone;
        public ExpressionTemplate angle;
        /** Counted up by the angle every frame. */
        public String inout;
        public Map<String, String> out;
    }

    static
    {
        KumoRegistry.registerDriver(KumoDriver.of("test:raise", RaiseTemplate.class, DriverApiTest::bind));
    }

    private static DriverEvaluator bind(RaiseTemplate template, DriverBindArgs args) throws MalformedKumoTemplateException
    {
        int bone = args.bone("bone", template.bone);
        NumberInput angle = args.number("angle", template.angle);
        StateHandle total = args.inout("inout", template.inout);
        StateHandle frames = args.outputs(template.out, "frames").get("frames");
        FloatSlot count = args.slot("count", 0);
        Quaternion rotation = new Quaternion();
        return (context, pose) -> {
            count.set(context, count.get(context) + 1);
            total.set(context, total.get(context) + angle.get(context));
            if (frames != null) frames.set(context, count.get(context));
            PoseMath.axisAngleDegrees(1, 0, 0, angle.get(context) * count.get(context), rotation);
            pose.rotate(bone, rotation, Pose.Space.OVERRIDE);
        };
    }

    /** The arm's angle about X, in degrees. */
    private static float angle(TestSubject subject, String bone)
    {
        Quaternion q = subject.target(bone);
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    private static String animator(String raise, int restartAfter)
    {
        return "{'formatVersion': 2, '@define': {'total': {'state': 0}, 'frames': {'state': 0}}, 'layers': [{'defaultOnEntry': 'a', "
                + "'nodes': {'a': {'core:pose': {'pose': [{'test:raise': " + raise + "}, "
                + "{'core:axis_rotate': {'bone': 'leg', 'axis': 'X', 'angle': {'add': ['animator.total', {'mul': ['animator.frames', 10]}]}}, '@space': 'OVERRIDE'}]}, "
                + "'@connections': [{'when': {'ge': ['nodeTicksElapsed', " + restartAfter + "]}, 'then': 'a'}]}}}]}";
    }

    @Test
    public void aDriverPosesStepsItsStateAndPublishesItsOutputs() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(animator("{'bone': 'arm', 'angle': 10, 'inout': 'animator.total', 'out': {'frames': 'animator.frames'}}", 100));
        TestSubject subject = new TestSubject("arm", "leg");
        animator.update(subject, 1F);
        assertEquals(10, angle(subject, "arm"), 1e-3);
        animator.update(subject, 1F);
        assertEquals(20, angle(subject, "arm"), 1e-3);
        // total 20, frames 2.
        assertEquals(40, angle(subject, "leg"), 1e-2);
    }

    @Test
    public void aDriversDeclaredStateStartsOverWithItsNode() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(animator("{'bone': 'arm', 'angle': 10, 'inout': 'animator.total'}", 2));
        TestSubject subject = new TestSubject("arm", "leg");
        float highest = 0;
        boolean restarted = false;
        for (int i = 0; i < 6; i++)
        {
            animator.update(subject, 1F);
            float a = angle(subject, "arm");
            if (a < highest - 1e-3) restarted = true;
            highest = Math.max(highest, a);
        }
        assertTrue("the count never started over", restarted);
    }

    @Test
    public void aDriversMistakesAreInItsOwnWords()
    {
        expectError("{'bone': 'arm', 'inout': 'animator.total'}", "'test:raise' needs 'angle'.");
        expectError("{'bone': 'arm', 'angle': 1}", "'test:raise' needs 'inout' (the state it steps).");
        expectError("{'bone': 'arm', 'angle': 1, 'inout': 'animator.total', 'out': {'laps': 'animator.frames'}}", "'test:raise' has no output 'laps' (it has frames).");
        expectError("{'bone': 'arm', 'angle': true, 'inout': 'animator.total'}", "'test:raise' 'angle': Expected a number");
    }

    private static void expectError(String raise, String message)
    {
        try
        {
            TestSubject.instance(animator(raise, 100));
            fail("Loads: " + raise);
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

}
