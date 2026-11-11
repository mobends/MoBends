package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's and a layer's smoothing, on the channels of a bone an item smooths only some of. */
public class SmoothingChannelsTest
{

    /** One node that turns and moves {@code root}, with the item smoothing {@code itemSmoothing}, under the given layer and node smoothing. */
    private static KumoAnimatorState animator(String itemSmoothing, String layerSmoothing, String nodeSmoothing) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', "
                + (layerSmoothing == null ? "" : "'smoothing': " + layerSmoothing + ", ")
                + "'nodes': {'a': {'core:pose': {'pose': ["
                + "{'core:rotate': {'bone': 'root', 'axis': 'y', 'angle': 90}, '@smoothing': " + itemSmoothing + "}, "
                + "{'core:translate': {'bone': 'root', 'x': 4}}]"
                + (nodeSmoothing == null ? "" : ", 'smoothing': " + nodeSmoothing)
                + "}}}}]}").replace('\'', '"'));
    }

    @Test
    public void aNodeSmoothsThePositionOfABoneWhoseRotationAnItemSmooths() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'root': {'rotation': 0}}", null, "{'root': 10}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(10F, subject.root.position.smoothing.x, 0F);
        assertEquals(10F, subject.root.position.smoothing.y, 0F);
        assertEquals(10F, subject.root.position.smoothing.z, 0F);
    }

    @Test
    public void aNodeSmoothsTheRotationOfABoneWhosePositionAnItemSmooths() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'root': {'position': 2}}", null, "{'root': 10}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(10F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(2F, subject.root.position.smoothing.x, 0F);
    }

    @Test
    public void aLayerSmoothsThePositionOfABoneWhoseRotationAnItemSmooths() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'root': {'rotation': 0}}", "{'root': 10}", null);
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(10F, subject.root.position.smoothing.x, 0F);
    }

    @Test
    public void theDefaultSmoothsThePositionOfABoneWhoseRotationAnItemSmooths() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'root': {'rotation': 0}}", null, "{'@default': 10}");
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);

        assertEquals(0F, subject.root.rotation.getSmoothing(), 0F);
        assertEquals(10F, subject.root.position.smoothing.x, 0F);
    }

}
