package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * When a layer's scopes are created, updated and disposed as it moves between nodes and machines:
 * what each list sees, and the order they run in.
 */
public class LayerLifecycleTest
{

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
    }

    /** Statements noting the step {@code name} ran at (animator.name), then moving the step on. */
    private static String mark(String name)
    {
        return "{'set': ['animator." + name + "', 'animator.step']}, {'set': ['animator.step', {'add': ['animator.step', 1]}]}";
    }

    /** A statement counting into animator.name. */
    private static String count(String name)
    {
        return "{'set': ['animator." + name + "', {'add': ['animator." + name + "', 1]}]}";
    }

    /**
     * The animator of {@code layer} (one layer's JSON), with an animator state per name in
     * {@code names} (each 0, and animator.step from 1), and a second layer posing a bone per name
     * by its value (in degrees about X), so they can be read whatever the first layer's crossfades do.
     */
    private static KumoAnimatorState animator(String layer, String... names) throws MalformedKumoTemplateException
    {
        StringBuilder define = new StringBuilder("'step': {'state': 1}");
        StringBuilder probe = new StringBuilder();
        for (String name : names)
        {
            define.append(", '").append(name).append("': {'state': 0}");
            probe.append(probe.length() == 0 ? "" : ", ").append("{'core:rotate': {'bone': '").append(name).append("', 'axis': 'x', 'angle': 'animator.")
                    .append(name).append("'}, '@space': 'override'}");
        }
        return TestSubject.instance(("{'formatVersion': 2, '@define': {" + define + "}, 'layers': [" + layer + ", "
                + "{'defaultOnEntry': 'probe', 'nodes': {'probe': {'core:pose': {'pose': [" + probe + "]}}}}]}").replace('\'', '"'));
    }

    /** The value animator.name had when the probe layer posed it. */
    private static float read(TestSubject subject, String name)
    {
        Quaternion q = subject.target(name);
        return Math.round(Math.toDegrees(2 * Math.atan2(q.x, q.w)));
    }

    private static void frames(KumoAnimatorState animator, TestSubject subject, int count) throws MalformedKumoTemplateException
    {
        for (int i = 0; i < count; i++)
        {
            animator.update(subject, 1F);
        }
    }

    @Test
    public void aNodesClockStartsOverBeforeItsScopeIsCreated() throws MalformedKumoTemplateException
    {
        // a notes its clock in a constant and in its enter list, leaves for b on its third frame and comes straight back.
        KumoAnimatorState animator = animator("{'defaultOnEntry': 'a', 'nodes': {"
                + "'a': {'core:pose': {}, '@define': {'start': {'constant': 'nodeTicksElapsed'}}, "
                + "  '@on': {'enter': [{'set': ['animator.entered', 'nodeTicksElapsed']}], 'update': [{'set': ['animator.start', 'node.start']}]}, "
                + "  '@connections': [{'when': {'ge': ['nodeTicksElapsed', 2]}, 'then': 'b'}]}, "
                + "'b': {'core:pose': {}, '@connections': [{'when': true, 'then': 'a'}]}}}", "entered", "start");
        TestSubject subject = new TestSubject("entered", "start");
        frames(animator, subject, 4);
        assertEquals("a", animator.getCurrentNodes().get(0));
        assertEquals(0, read(subject, "entered"), 0);
        assertEquals(0, read(subject, "start"), 0);
    }

    @Test
    public void aMachineKeepsUpdatingWhileItsNodeFadesOut() throws MalformedKumoTemplateException
    {
        // a, in m, crossfades out of m over 3 ticks on frame 4.
        KumoAnimatorState animator = animator("{'defaultOnEntry': 'm', 'nodes': {'out': {'core:pose': {}}}, "
                + "'machines': {'m': {'@on': {'update': [" + count("frames") + "], 'exit': [" + count("exits") + "]}, "
                + "  'nodes': {'a': {'core:pose': {}, '@connections': [{'when': {'gt': ['nodeTicksElapsed', 2]}, 'then': 'out', 'transitionDuration': 3}]}}}}}",
                "frames", "exits");
        TestSubject subject = new TestSubject("frames", "exits");
        frames(animator, subject, 5);
        // Frames 4 to 6, a fades out: m still exists, and updates.
        assertEquals(5, read(subject, "frames"), 0);
        assertEquals(0, read(subject, "exits"), 0);
        // The crossfade ended once frame 6 was posed: m was left then, once.
        frames(animator, subject, 3);
        assertEquals(6, read(subject, "frames"), 0);
        assertEquals(1, read(subject, "exits"), 0);
    }

    @Test
    public void aConnectionIntoTheMachineItIsInLeavesItFirst() throws MalformedKumoTemplateException
    {
        // a, in m inside outer, leads into outer again when GO holds.
        KumoAnimatorState animator = animator("{'defaultOnEntry': 'outer', 'nodes': {'idle': {'core:pose': {}}}, 'machines': {'outer': {"
                + "  '@on': {'enter': [" + count("outerEnters") + ", " + mark("outerEntered") + "], 'exit': [" + count("outerExits") + ", " + mark("outerExited") + "]}, "
                + "  'machines': {'m': {'@on': {'enter': [" + count("mEnters") + "], 'exit': [" + count("mExits") + ", " + mark("mExited") + "]}, "
                + "    'nodes': {'a': {'core:pose': {}, '@connections': [{'when': 'GO', 'then': 'outer', 'do': [" + mark("done") + "]}]}}}}}}}",
                "outerEnters", "outerExits", "outerEntered", "outerExited", "mEnters", "mExits", "mExited", "done");
        TestSubject subject = new TestSubject("outerEnters", "outerExits", "outerEntered", "outerExited", "mEnters", "mExits", "mExited", "done");
        subject.states.put("GO", false);
        frames(animator, subject, 2);
        subject.states.put("GO", true);
        frames(animator, subject, 1);
        // Left, the inner machine first, before the connection's do; then created afresh.
        assertEquals(2, read(subject, "outerEnters"), 0);
        assertEquals(1, read(subject, "outerExits"), 0);
        assertEquals(2, read(subject, "mEnters"), 0);
        assertEquals(1, read(subject, "mExits"), 0);
        assertTrue(read(subject, "mExited") < read(subject, "outerExited"));
        assertTrue(read(subject, "outerExited") < read(subject, "done"));
        assertTrue(read(subject, "done") < read(subject, "outerEntered"));
    }

    /**
     * Machine m holds machine inner, which holds node a; out is outside both. Every list notes the
     * step it ran at, and inner counts its frames.
     */
    private static final String NESTED = "{'defaultOnEntry': 'm', "
            + "'nodes': {'out': {'core:pose': {}, '@connections': [{'when': 'GO', 'then': 'inner', 'transitionDuration': 2, 'do': [" + mark("goDone") + "]}]}}, "
            + "'machines': {'m': {'@on': {'enter': [" + mark("mEntered") + "], 'exit': [" + mark("mExited") + ", " + count("mExits") + "]}, "
            + "  'machines': {'inner': {'@on': {'enter': [" + mark("innerEntered") + "], 'update': [" + count("innerFrames") + "], "
            + "    'exit': [" + mark("innerExited") + ", " + count("innerExits") + "]}, "
            + "    'nodes': {'a': {'core:pose': {}, '@on': {'exit': [" + mark("aExited") + "]}, "
            + "      '@connections': [{'when': 'OUT', 'then': 'out', 'transitionDuration': %s, 'do': [" + mark("outDone") + "]}]}}}}}}}";

    private static final String[] NESTED_NAMES = { "goDone", "mEntered", "mExited", "mExits", "innerEntered", "innerFrames", "innerExited", "innerExits", "aExited", "outDone" };

    @Test
    public void anImmediateTransitionLeavesTheMachinesInnermostFirstBeforeItsDo() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(String.format(NESTED, 0), NESTED_NAMES);
        TestSubject subject = new TestSubject(NESTED_NAMES);
        subject.states.put("GO", false);
        subject.states.put("OUT", false);
        frames(animator, subject, 2);
        // Entered from the outermost in.
        assertTrue(read(subject, "mEntered") < read(subject, "innerEntered"));
        subject.states.put("OUT", true);
        frames(animator, subject, 1);
        // The node, the inner machine, the outer one, then the connection's do.
        assertTrue(read(subject, "aExited") < read(subject, "innerExited"));
        assertTrue(read(subject, "innerExited") < read(subject, "mExited"));
        assertTrue(read(subject, "mExited") < read(subject, "outDone"));
        assertEquals(1, read(subject, "innerExits"), 0);
        assertEquals(1, read(subject, "mExits"), 0);
    }

    @Test
    public void aNestedMachineEnteredAgainWhileItsNodeFadesOutStartsOverAndKeepsUpdating() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(String.format(NESTED, 5), NESTED_NAMES);
        TestSubject subject = new TestSubject(NESTED_NAMES);
        subject.states.put("GO", false);
        subject.states.put("OUT", true);
        frames(animator, subject, 2);
        // a fades out over 5 ticks: m and inner still exist.
        assertEquals(0, read(subject, "innerExits"), 0);
        subject.states.put("OUT", false);
        subject.states.put("GO", true);
        frames(animator, subject, 1);
        subject.states.put("GO", false);
        // Both were left, the inner one first, before the do; then created again from the outermost in.
        assertEquals(1, read(subject, "innerExits"), 0);
        assertEquals(1, read(subject, "mExits"), 0);
        assertTrue(read(subject, "innerExited") < read(subject, "mExited"));
        assertTrue(read(subject, "mExited") < read(subject, "goDone"));
        assertTrue(read(subject, "goDone") < read(subject, "mEntered"));
        assertTrue(read(subject, "mEntered") < read(subject, "innerEntered"));
        // Long after every crossfade ended, inner still updates every frame, and was never left again.
        float before = read(subject, "innerFrames");
        frames(animator, subject, 10);
        assertEquals(before + 10, read(subject, "innerFrames"), 0);
        assertEquals(1, read(subject, "innerExits"), 0);
        assertEquals(1, read(subject, "mExits"), 0);
    }

}
