package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ScaleDriverTest
{

    private static String layer(String pose)
    {
        return "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + pose + "]}}}}";
    }

    private static String animatorJson(String pose)
    {
        return "{'formatVersion': 2, 'layers': [" + layer(pose) + "]}";
    }

    private static KumoAnimatorState animator(String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(animatorJson(pose));
    }

    /** A trusted animator posing {@code trusted}, with an untrusted extension posing {@code untrusted}, under {@code limits}. */
    private static KumoAnimatorState limited(String trusted, String untrusted, AnimationLimits limits) throws MalformedKumoTemplateException
    {
        AnimatorTemplate base = TestSubject.animator(animatorJson(trusted));
        AnimatorTemplate extension = TestSubject.animator(animatorJson(untrusted));
        KumoAnimatorState state = new KumoAnimatorState(base, true, Collections.singletonList(extension), Collections.singletonList(false), name -> null);
        state.setLimits(limits);
        return state;
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    @Test
    public void scalesAPart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:scale': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 2, 3, subject.scales.get("arm"));
    }

    @Test
    public void allScalesTheAxesNotGivenTheirOwn() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:scale': {'bone': 'arm', 'all': 2, 'y': 3}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(2, 3, 2, subject.scales.get("arm"));
    }

    @Test
    public void multipliesInPreSpaceByDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:scale': {'bone': 'arm', 'x': 2, 'y': 2, 'z': 2}, '@space': 'override'}, "
                + "{'core:scale': {'bone': 'arm', 'y': 3}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(2, 6, 2, subject.scales.get("arm"));
    }

    @Test
    public void anOverrideAxisLeftOutKeepsWhatTheLayersBelowGaveIt() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{'formatVersion': 2, 'layers': ["
                + layer("{'core:scale': {'bone': 'arm', 'x': 2, 'y': 1, 'z': 3}, '@space': 'override'}") + ", "
                + layer("{'core:scale': {'bone': 'arm', 'y': 5}, '@space': 'override'}") + "]}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(2, 5, 3, subject.scales.get("arm"));
    }

    @Test
    public void anOverrideAxisLeftOutWithNothingBeneathIsAtRest() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:scale': {'bone': 'arm', 'y': 5}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");
        subject.scales.get("arm").set(2, 1, 3);

        animator.update(subject, 1F);

        // Not the part's scale from before: the rest scale.
        assertVector(1, 5, 1, subject.scales.get("arm"));
    }

    @Test
    public void aRelativeScaleBuildsOnTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{'formatVersion': 2, 'layers': ["
                + layer("{'core:scale': {'bone': 'arm', 'all': 2}, '@space': 'override'}") + ", "
                + layer("{'core:scale': {'bone': 'arm', 'x': 3}}") + "]}");
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 3; frame++)
        {
            animator.update(subject, 1F);
            // The same every frame: the relative layer doesn't compound on last frame's result.
            assertVector(6, 2, 2, subject.scales.get("arm"));
        }
    }

    @Test
    public void aTransitionCrossfadesTheScale() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'small', 'nodes': {"
                + "'small': {'core:pose': {'pose': [{'core:scale': {'bone': 'arm', 'all': 1}, '@space': 'override'}]}, "
                + "'@connections': [{'when': {'gt': ['nodeTicksElapsed', 0]}, 'then': 'big', 'transitionDuration': 10, 'transitionEasing': 'linear'}]}, "
                + "'big': {'core:pose': {'pose': [{'core:scale': {'bone': 'arm', 'all': 3}, '@space': 'override'}]}}}}]}");
        TestSubject subject = new TestSubject("arm");

        // The first frame, then the one the transition starts on (t = 0), then halfway through.
        for (int frame = 0; frame < 7; frame++)
        {
            animator.update(subject, 1F);
        }
        assertVector(2, 2, 2, subject.scales.get("arm"));

        for (int frame = 0; frame < 10; frame++)
        {
            animator.update(subject, 1F);
        }
        assertVector(3, 3, 3, subject.scales.get("arm"));
    }

    @Test
    public void theEntitysOwnBonesCantBeScaled()
    {
        try
        {
            animator("{'core:scale': {'bone': 'root', 'all': 2}}");
            fail("root was scaled");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("root"));
        }
    }

    @Test
    public void aScaleIsntSmoothed()
    {
        try
        {
            animator("{'core:scale': {'bone': 'arm', 'all': 2}, '@smoothing': {'arm': 4}}");
            fail("a smoothed scale loaded");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("smoothed"));
        }
    }

    @Test
    public void untrustedScalesStayWithinAFactorOfTheTrustedOnes() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = limited("{'core:scale': {'bone': 'arm', 'all': 2}, '@space': 'override'}",
                "{'core:scale': {'bone': 'arm', 'x': 100, 'y': 0, 'z': -1}, '@space': 'override'}", new AnimationLimits(4, 16, 1.5F));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        // Up to 1.5 times the trusted 2; down to it over 1.5, and neither nothing nor turned inside out.
        assertVector(3, 2 / 1.5F, 2 / 1.5F, subject.scales.get("arm"));
    }

    @Test
    public void untrustedRelativeScalesDontCompound() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = limited("", "{'core:scale': {'bone': 'arm', 'all': 1.2}}", new AnimationLimits(4, 16, 1.5F));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
            assertVector(1.2F, 1.2F, 1.2F, subject.scales.get("arm"));
        }
    }

    @Test
    public void aScaleNothingWritesGoesBackToRest() throws MalformedKumoTemplateException
    {
        // Nothing scales the arm (it is only turned).
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 10}}");
        TestSubject subject = new TestSubject("arm");
        subject.scales.get("arm").set(10, 0, Float.NaN);

        animator.update(subject, 1F);

        assertVector(1, 1, 1, subject.scales.get("arm"));
    }

    @Test
    public void aMaxScaleOfOneForbidsScaling() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = limited("", "{'core:scale': {'bone': 'arm', 'all': 5}, '@space': 'override'}", new AnimationLimits(4, 16, 1F));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 1, 1, subject.scales.get("arm"));
    }

    @Test
    public void trustedScalesAreNotLimited() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = limited("{'core:scale': {'bone': 'arm', 'all': 10}, '@space': 'override'}", "", new AnimationLimits(4, 16, 1.5F));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(10, 10, 10, subject.scales.get("arm"));
    }

}
