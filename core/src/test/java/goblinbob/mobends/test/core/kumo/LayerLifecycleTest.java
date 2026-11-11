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

}
