package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's {@code enterPose} that scales a part. */
public class EnterPoseScaleTest
{

    private static String node(String enter, String pose)
    {
        return "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'enterPose': [" + enter + "], 'pose': [" + pose + "]}}}}";
    }

    /** One layer, whose node enters with {@code enter} and poses {@code pose}. */
    private static KumoAnimatorState animator(String enter, String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [" + node(enter, pose) + "]}").replace('\'', '"'));
    }

    /** A lower layer that poses {@code lower}, and one above whose node enters with {@code enter} and poses nothing. */
    private static KumoAnimatorState onTopOf(String lower, String enter) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + node(enter, "") + "]}").replace('\'', '"'));
    }

    private static String scale(String axes, String space)
    {
        return "{'core:scale': {'bone': 'arm', " + axes + "}, '@space': '" + space + "'}";
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    @Test
    public void aScaleOnlyTheEntryWritesHoldsOnTheEntryFrame() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(scale("'all': 0.5", "override"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        assertVector(0.5F, 0.5F, 0.5F, subject.scales.get("arm"));

        // Nothing scales it after that, so it's back as modelled.
        animator.update(subject, 1F);
        assertVector(1, 1, 1, subject.scales.get("arm"));
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutAreTheLayersBelows() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(scale("'x': 1.2, 'y': 1.3, 'z': 1.4", "override"), scale("'x': 0.5", "override"));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        // (0.5, 1.3, 1.4), not (0.5, 1, 1).
        assertVector(0.5F, 1.3F, 1.4F, subject.scales.get("arm"));

        animator.update(subject, 1F);
        assertVector(1.2F, 1.3F, 1.4F, subject.scales.get("arm"));
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutWithNothingBelowAreAsModelled() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(scale("'x': 0.5", "override"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(0.5F, 1, 1, subject.scales.get("arm"));
    }

    @Test
    public void aRelativeEntryScalesWhatTheLayersBelowHave() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(scale("'all': 1.2", "override"), scale("'all': 0.5", "pre"));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        assertVector(0.6F, 0.6F, 0.6F, subject.scales.get("arm"));

        animator.update(subject, 1F);
        assertVector(1.2F, 1.2F, 1.2F, subject.scales.get("arm"));
    }

    @Test
    public void thePosesOwnScaleWins() throws MalformedKumoTemplateException
    {
        // A scale isn't smoothed, so the entry has nothing to start it from.
        KumoAnimatorState animator = animator(scale("'all': 0.5", "override"), scale("'all': 1.2", "override"));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1.2F, 1.2F, 1.2F, subject.scales.get("arm"));
    }

}
