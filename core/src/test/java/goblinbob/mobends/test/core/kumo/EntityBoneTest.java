package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/** The entity's own bones, {@code root} and {@code center}: a rotation and a position each. */
public class EntityBoneTest
{

    /** A root track of root motion alone, and a root track that turns the root alone. */
    private static final String MOTION_CLIP = "{'bones': {'root': {'keyframes': [{'position': [0, 4, 0]}]}}}";
    private static final String TURN_CLIP = "{'bones': {'root': {'keyframes': [{'rotation': [0, 0.70710677, 0, 0.70710677]}]}}}";
    private static final String BOTH_CLIP = "{'bones': {'root': {'keyframes': [{'position': [0, 4, 0], 'rotation': [0, 0.70710677, 0, 0.70710677]}]}}}";

    private static KumoAnimatorState animator(String pose, String clip) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + pose + "]}}}}]}").replace('\'', '"'),
                Collections.singletonMap("clip", clip.replace('\'', '"')));
    }

    private static void assertTurn(float degrees, Quaternion actual)
    {
        Quaternion expected = TestSubject.axisAngle(0, 1, 0, degrees);
        assertEquals("y", expected.y, actual.y, 1e-4F);
        assertEquals("w", expected.w, actual.w, 1e-4F);
    }

    @Test
    public void aRootTrackOfRootMotionLeavesTheRotationAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'root', 'axis': 'y', 'angle': 30}, '@space': 'override'}, "
                + "{'core:clip': {'animationKey': 'clip'}}", MOTION_CLIP);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertEquals(4, subject.root.position.end.y, 1e-4F);
        assertTurn(30, subject.root.rotation.getEnd());
    }

    @Test
    public void aRootTrackThatTurnsLeavesThePositionAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 0, 'y': 2, 'z': 0}, '@space': 'override'}, "
                + "{'core:clip': {'animationKey': 'clip'}}", TURN_CLIP);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertEquals(2, subject.root.position.end.y, 1e-4F);
        assertTurn(90, subject.root.rotation.getEnd());
    }

    @Test
    public void aClipItemCanApplyOneChannelOfTheRoot() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 0, 'y': 2, 'z': 0}, '@space': 'override'}, "
                + "{'core:rotate': {'bone': 'root', 'axis': 'y', 'angle': 30}, '@space': 'override'}, "
                + "{'core:clip': {'animationKey': 'clip', 'bones': ['root.rotation']}}", BOTH_CLIP);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertEquals("the position isn't the clip's", 2, subject.root.position.end.y, 1e-4F);
        assertTurn(90, subject.root.rotation.getEnd());
    }

    @Test
    public void aRotateItemSnapsOnlyTheRotation() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 0, 'y': 8, 'z': 0}, '@space': 'override', '@smoothing': {'root': 4}}, "
                + "{'core:rotate': {'bone': 'root', 'axis': 'y', 'angle': 30}, '@space': 'override', '@snap': true}", MOTION_CLIP);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertTurn(30, subject.root.rotation.getSmooth());
        assertEquals("the position still eases in", 8, subject.root.position.end.y, 1e-4F);
        subject.root.update(1F);
        assertEquals(2, subject.root.position.getY(), 1e-3F);
    }

    @Test
    public void aSmoothingCanGiveTheRotationAndThePositionApart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:clip': {'animationKey': 'clip'}, '@smoothing': {'root': {'rotation': 0, 'position': 4}}}", BOTH_CLIP);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        subject.root.update(1F);

        assertTurn(90, subject.root.rotation.getSmooth());
        assertEquals(1, subject.root.position.getY(), 1e-3F);
    }

}
