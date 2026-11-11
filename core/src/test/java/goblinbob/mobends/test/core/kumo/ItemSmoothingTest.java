package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/** An item's {@code @smoothing}, and its {@code @default}. */
public class ItemSmoothingTest
{

    /** One node posing the items, with {@code node.ticks} a constant 10. */
    private static KumoAnimatorState animator(String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': ["
                + pose + "]}, '@define': {'ticks': {'constant': 10}}}}}]}");
    }

    /** How far the head's offset got toward 10 in one tick. */
    private static float firstTick(String smoothing) throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'head', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': " + smoothing + "}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);
        subject.updateOffsets(1F);
        return subject.offsets.get("head").y;
    }

    @Test
    public void aNumericDefaultSmoothsWhatTheItemWrites() throws MalformedKumoTemplateException
    {
        assertEases(firstTick("{'@default': 10}"));
    }

    @Test
    public void anExpressionDefaultSmoothsWhatTheItemWrites() throws MalformedKumoTemplateException
    {
        assertEases(firstTick("{'@default': 'node.ticks'}"));
    }

    private static void assertEases(float first)
    {
        assertTrue("on its way: " + first, first > 0 && first < 10);
    }

}
