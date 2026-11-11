package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's {@code enterPose} that translates a bone on some axes only. */
public class EnterPoseAxesTest
{

    /** A lower layer that poses {@code lower}, and one above whose node enters with {@code enter} and poses {@code pose}. */
    private static KumoAnimatorState animator(String lower, String enter, String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + "{'defaultOnEntry': 'b', 'nodes': {'b': {'core:pose': {'enterPose': [" + enter + "], 'pose': [" + pose + "]}}}}]}")
                .replace('\'', '"'));
    }

    private static void assertVector(float x, float y, float z, IVec3fRead actual)
    {
        assertEquals("x", x, actual.getX(), 1e-4F);
        assertEquals("y", y, actual.getY(), 1e-4F);
        assertEquals("z", z, actual.getZ(), 1e-4F);
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutStartFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 10}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override', '@smoothing': {'root': {'position': 10}}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        // From (10, 20, 30), not (10, NaN, NaN).
        assertVector(10, 20, 30, subject.root.position.start);
        assertVector(0, 20, 30, subject.root.position.end);
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutOfABoneTheNodeDoesntPoseAreTheLayersBelows() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 10}, '@space': 'override'}", "");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertVector(10, 20, 30, subject.root.position.end);
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutWithNothingBelowAreAtRest() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("",
                "{'core:translate': {'bone': 'root', 'x': 10}, '@space': 'override'}",
                "{'core:translate': {'bone': 'root', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override', '@smoothing': {'root': {'position': 10}}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertVector(10, 0, 0, subject.root.position.start);
    }

    @Test
    public void theAxesAnOverrideEntryLeavesOutOfAPartsOffsetStartFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override'}",
                "{'core:translate': {'bone': 'arm', 'x': 10}, '@space': 'override'}",
                "{'core:translate': {'bone': 'arm', 'x': 0, 'y': 20, 'z': 30}, '@space': 'override', '@smoothing': {'arm': 10}}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertVector(10, 20, 30, subject.smoothOffsets.get("arm").start);
        assertVector(0, 20, 30, subject.smoothOffsets.get("arm").end);
    }

}
