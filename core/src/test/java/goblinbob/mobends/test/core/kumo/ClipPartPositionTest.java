package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** A clip's {@code @smoothing}, {@code @snap} and {@code @vectorModes} on a model part's position: its offset. */
public class ClipPartPositionTest
{

    /** One key: the head moved by 10 along x (a part's clip position is negated). */
    private static final String CLIP = "{'bones': {'head': {'keyframes': [{'position': [-10, 0, 0], 'rotation': [0, 0, 0, 1]}]}}}";

    private static KumoAnimatorState animator(String nodeSmoothing, String modifiers) throws MalformedKumoTemplateException
    {
        String smoothing = nodeSmoothing == null ? "" : "'smoothing': " + nodeSmoothing + ", ";
        return animatorOf(smoothing + "'pose': [{'core:clip': {'animationKey': 'clip'}" + modifiers + "}]");
    }

    /** A translate of the head that eases over 10 ticks, then a clip of the head's rotation alone, with {@code modifiers}. */
    private static KumoAnimatorState afterATranslate(String modifiers) throws MalformedKumoTemplateException
    {
        return animatorOf("'pose': [{'core:translate': {'bone': 'head', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'head': 10}}, "
                + "{'core:clip': {'animationKey': 'clip', 'bones': ['head.rotation']}" + modifiers + "}]");
    }

    /** A translate of the head that eases over 10 ticks, then a clip of the head and the arm applying the arm alone, with {@code modifiers}. */
    private static KumoAnimatorState besideATranslate(String modifiers) throws MalformedKumoTemplateException
    {
        String clip = "{'bones': {'head': {'keyframes': [{'position': [-10, 0, 0], 'rotation': [0, 0, 0, 1]}]}, "
                + "'arm': {'keyframes': [{'position': [0, 0, 0], 'rotation': [0, 0, 0, 1]}]}}}";
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {"
                + "'pose': [{'core:translate': {'bone': 'head', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'head': 10}}, "
                + "{'core:clip': {'animationKey': 'clip', 'bones': ['arm']}" + modifiers + "}]}}}}]}").replace('\'', '"'),
                Collections.singletonMap("clip", clip.replace('\'', '"')));
    }

    private static KumoAnimatorState animatorOf(String node) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {" + node + "}}}}]}")
                .replace('\'', '"'), Collections.singletonMap("clip", CLIP.replace('\'', '"')));
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    @Test
    public void withoutSmoothingThePartIsMovedAtOnce() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, "");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(10, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void theClipsSmoothingEasesThePart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, ", '@smoothing': {'head': 10}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);
        assertVector(0, 0, 0, subject.offsets.get("head"));
        subject.updateOffsets(1F);
        float first = subject.offsets.get("head").x;
        assertTrue("on its way: " + first, first > 0 && first < 10);

        for (int frame = 0; frame < 100; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertEquals("there", 10F, subject.offsets.get("head").x, 1e-2F);
    }

    @Test
    public void theClipsVectorModeSlidesThePart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, ", '@smoothing': {'head': 4}, '@vectorModes': {'head': 'slide'}");
        TestSubject subject = new TestSubject("head");

        // A linear tween of 4 ticks.
        for (int frame = 0; frame < 2; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(5, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void theNodesSmoothingEasesThePart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'head': 10}", "");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void theClipsSnapWinsOverTheNodesSmoothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'head': 10}", ", '@snap': true");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(10, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void theClipsSnapModeWinsOverTheNodesSmoothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'head': 10}", ", '@vectorModes': {'head': 'snap'}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(10, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void theClipsSmoothingWinsOverTheNodes() throws MalformedKumoTemplateException
    {
        // The node's 100 ticks would barely move it; the clip's 1 tick gets it there in one.
        KumoAnimatorState animator = animator("{'head': 100}", ", '@smoothing': {'head': 1}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);
        subject.updateOffsets(1F);

        assertVector(10, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void aRotationClipsSnapLeavesAnEarlierTranslateEasing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = afterATranslate(", '@snap': true");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void aRotationClipsSnapModeLeavesAnEarlierTranslateEasing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = afterATranslate(", '@vectorModes': {'head': 'snap'}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void aRotationClipsSmoothingLeavesAnEarlierTranslatesAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = afterATranslate(", '@smoothing': {'head': 0}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);
        subject.updateOffsets(1F);

        float first = subject.offsets.get("head").y;
        assertTrue("on its way: " + first, first > 0 && first < 10);
    }

    @Test
    public void aClipsSnapModeLeavesABoneItLeavesOutEasing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = besideATranslate(", '@vectorModes': {'head': 'snap'}");
        TestSubject subject = new TestSubject("head", "arm");

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.offsets.get("head"));
    }

    @Test
    public void aClipsSmoothingLeavesABoneItLeavesOutAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = besideATranslate(", '@smoothing': {'head': 0}");
        TestSubject subject = new TestSubject("head", "arm");

        animator.update(subject, 1F);
        subject.updateOffsets(1F);

        float first = subject.offsets.get("head").y;
        assertTrue("on its way: " + first, first > 0 && first < 10);
    }

}
