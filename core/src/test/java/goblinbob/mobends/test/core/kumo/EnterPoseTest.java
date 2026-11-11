package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's {@code enterPose}: where the bones start from when the node is entered. */
public class EnterPoseTest
{

    /** A lower layer that poses {@code lower}, and one above whose node enters with {@code enter} and poses {@code pose}. */
    private static KumoAnimatorState animator(String lower, String enter, String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + "{'defaultOnEntry': 'b', 'nodes': {'b': {'core:pose': {'enterPose': [" + enter + "], 'pose': [" + pose + "]}}}}]}")
                .replace('\'', '"'));
    }

    private static void assertRotation(Quaternion expected, Quaternion actual)
    {
        float dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals("rotation " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w, 1F, dot, 1e-4F);
    }

    @Test
    public void anAdditiveEntryStartsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 20}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 10}}",
                "{'core:translate': {'bone': 'root', 'x': 0}, '@space': 'override', '@smoothing': {'root': 10}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        // From 20 + 10, not from 10.
        assertEquals(30F, subject.root.position.start.x, 1e-4F);
        assertEquals(0F, subject.root.position.end.x, 1e-4F);
    }

    @Test
    public void anAdditiveEntryOfABoneTheNodeDoesntPoseSnapsOnTopOfTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 20}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 10}}", "");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(30F, subject.root.position.end.x, 1e-4F);
    }

    @Test
    public void aRelativeEntryRotationStartsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 20}, '@space': 'override'}",
                "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 10}}",
                "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 0}, '@space': 'override', '@smoothing': {'arm': 10}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        // From 20 + 10 degrees: the entry isn't dropped for being relative.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 30), subject.bones.get("arm").getSmooth());
        assertRotation(TestSubject.axisAngle(1, 0, 0, 0), subject.target("arm"));
    }

    @Test
    public void aRelativeEntryRotationOfABoneTheNodeDoesntPoseSnapsOnTopOfTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 20}, '@space': 'override'}",
                "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 10}}", "");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertRotation(TestSubject.axisAngle(1, 0, 0, 30), subject.target("arm"));
    }

    @Test
    public void anAbsoluteEntryStartsWhereItSays() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 20}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 10}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 0}, '@space': 'override', '@smoothing': {'root': 10}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(10F, subject.root.position.start.x, 1e-4F);
    }

    @Test
    public void anAdditiveEntryOffsetStartsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'y': 20}, '@space': 'override'}",
                "{'core:translate': {'bone': 'arm', 'y': 10}}",
                "{'core:translate': {'bone': 'arm', 'y': 0}, '@space': 'override', '@smoothing': {'arm': 10}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(30F, subject.smoothOffsets.get("arm").start.y, 1e-4F);
        assertEquals(0F, subject.smoothOffsets.get("arm").end.y, 1e-4F);
    }

    @Test
    public void anAdditiveEntryOffsetOfAPartTheNodeDoesntPoseSnapsOnTopOfTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'y': 20}, '@space': 'override'}",
                "{'core:translate': {'bone': 'arm', 'y': 10}}", "");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(30F, subject.offsets.get("arm").y, 1e-4F);
    }

}
