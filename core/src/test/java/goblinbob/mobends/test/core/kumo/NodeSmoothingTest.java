package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's and a layer's {@code smoothing}: a bone's own entries, and {@code @default} for the rest. */
public class NodeSmoothingTest
{

    private static KumoAnimatorState animator(String smoothing) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {"
                + "'pose': [{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 90}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}}, "
                + "{'core:rotate': {'bone': 'root', 'axis': 'y', 'angle': 90}}, "
                + "{'core:translate': {'bone': 'root', 'x': 4}}], "
                + "'smoothing': " + smoothing + "}}}}]}").replace('\'', '"'));
    }

    @Test
    public void aBonesOwnRotationSmoothingWinsOverTheDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'@default': 10, 'head': {'rotation': 0}}");
        TestSubject subject = new TestSubject("head", "arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.bones.get("head").getSmoothing(), 0F);
        assertEquals(10F, subject.bones.get("arm").getSmoothing(), 0F);
    }

    @Test
    public void theDefaultSmoothsTheChannelABonesOwnEntryLeavesOut() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'@default': 10, 'root': {'rotation': 0}}");
        TestSubject subject = new TestSubject("head", "arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(10F, subject.root.position.smoothing.x, 0F);
    }

    @Test
    public void aBonesOwnPositionSmoothingWinsOverTheDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'@default': 10, 'root': {'position': 2}}");
        TestSubject subject = new TestSubject("head", "arm");
        animator.update(subject, 1F);

        assertEquals(10F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(2F, subject.root.position.smoothing.x, 0F);
    }

    @Test
    public void anItemsSmoothingWinsOverTheNodesAndTheDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {"
                + "'pose': [{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 90}, '@smoothing': {'head': 3}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}, '@smoothing': {'arm': 4}}], "
                + "'smoothing': {'@default': 10, 'head': {'rotation': 0}}}}}}]}").replace('\'', '"'));
        TestSubject subject = new TestSubject("head", "arm");
        animator.update(subject, 1F);

        assertEquals(3F, subject.bones.get("head").getSmoothing(), 0F);
        assertEquals(4F, subject.bones.get("arm").getSmoothing(), 0F);
    }

    @Test
    public void aPartsOffsetIsItsPositionForTheDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {"
                + "'pose': [{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 90}}, {'core:translate': {'bone': 'head', 'y': 4}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}}, {'core:translate': {'bone': 'arm', 'y': 4}}], "
                + "'smoothing': {'@default': 10, 'head': {'rotation': 0}, 'arm': {'position': 2}}}}}}]}").replace('\'', '"'));
        TestSubject subject = new TestSubject("head", "arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.bones.get("head").getSmoothing(), 0F);
        assertEquals(10F, subject.smoothOffsets.get("head").smoothing.y, 0F);
        assertEquals(10F, subject.bones.get("arm").getSmoothing(), 0F);
        assertEquals(2F, subject.smoothOffsets.get("arm").smoothing.y, 0F);
    }

}
