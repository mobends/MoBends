package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TranslateDriverTest
{

    private static KumoAnimatorState animator(String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + pose + "]}}}}]}");
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    @Test
    public void movesAPartsOffset() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 2, 3, subject.offsets.get("arm"));
    }

    @Test
    public void movesAnEntityLevelVector() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 1, 'y': 2, 'z': 3}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 2, 3, subject.root.end);
    }

    @Test
    public void anAxisLeftOutKeepsWhatThePartHasSoFar() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}}, "
                + "{'core:translate': {'bone': 'arm', 'y': 5}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 5, 3, subject.offsets.get("arm"));
    }

    @Test
    public void anAxisLeftOutKeepsThePartsCurrentOffset() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'y': 5}}");
        TestSubject subject = new TestSubject("arm");
        subject.offsets.get("arm").set(1, 0, 3);
        subject.offsetSmoothings.get("arm").set(1, 0, 3);

        animator.update(subject, 1F);

        assertVector(1, 5, 3, subject.offsets.get("arm"));
    }

    @Test
    public void aRelativeAxisLeftOutMovesByNothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}}, "
                + "{'core:translate': {'bone': 'arm', 'y': 5}, '@space': 'pre'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 7, 3, subject.offsets.get("arm"));
    }

    @Test
    public void aDampedOffsetIsSmoothed() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@damping': {'arm': 0.25}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        assertVector(0, 0, 0, subject.offsets.get("arm"));
        subject.updateOffsets(1F);
        float first = subject.offsets.get("arm").y;
        assertTrue("on its way: " + first, first > 0 && first < 10);

        for (int frame = 0; frame < 40; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aDampedOffsetSnaps() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@damping': {'arm': 0.25}, '@snap': true}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aDampedOffsetSlides() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@damping': {'arm': 0.25}, '@vectorModes': {'arm': 'slide'}}");
        TestSubject subject = new TestSubject("arm");

        // A linear tween of 1 / 0.25 = 4 ticks.
        for (int frame = 0; frame < 2; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 5, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aDampedTranslateLeavesTheRotationsDampingAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@damping': {'arm': 0.25}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        subject.bones.get("arm").update(1F);

        // The rotation's rate is still 1 per tick: there in one tick.
        Quaternion shown = subject.bones.get("arm").getSmooth();
        Quaternion target = subject.target("arm");
        float dot = Math.abs(shown.x * target.x + shown.y * target.y + shown.z * target.z + shown.w * target.w);
        assertEquals(1F, dot, 1e-4F);
    }

    @Test
    public void anUndampedOffsetIsAppliedAsIs() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}, '@space': 'override', '@damping': {'arm': 0.1}}, "
                + "{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        // A rotation's damping doesn't reach the offset.
        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

}
