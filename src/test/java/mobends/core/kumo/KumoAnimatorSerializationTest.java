package mobends.core.kumo;

import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.StandardKeyframeNodeTemplate;
import mobends.mock.MockSerialContext;
import mobends.mock.MockSerialQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class KumoAnimatorSerializationTest
{

    ISerialContext createMockSerialContext()
    {
        MockSerialContext context = new MockSerialContext();
        context.keyframeNodeRegistry.register("core:standard", StandardKeyframeNodeTemplate::deserialize);

        return context;
    }

    KeyframeNodeTemplate createMockKeyframeNodeTemplate()
    {
        return new StandardKeyframeNodeTemplate("animation", rand.nextInt(), rand.nextFloat(), rand.nextBoolean());
    }

    LayerTemplate createMockLayerTemplate()
    {
        return new KeyframeLayerTemplate(0, null, new KeyframeNodeTemplate[] {
                createMockKeyframeNodeTemplate(),
        });
    }

    AnimatorTemplate createMockAnimatorTemplate()
    {
        AnimatorTemplate animatorTemplate = new AnimatorTemplate();

        StandardKeyframeNodeTemplate nodeOne = new StandardKeyframeNodeTemplate("hello:there", 0, 1, false);

        KeyframeLayerTemplate sampleLayer = new KeyframeLayerTemplate(0, null, new KeyframeNodeTemplate[] {
                nodeOne
        });

        animatorTemplate.layers = new LayerTemplate[] { sampleLayer };

        return animatorTemplate;
    }

    private Random rand;
    private MockSerialQueue queue;

    @Before
    public void init()
    {
        this.rand = new Random();
        this.queue = new MockSerialQueue();
    }

    @Test
    public void keyframeNodeTemplate()
    {
        ISerialContext context = createMockSerialContext();
        KeyframeNodeTemplate original = createMockKeyframeNodeTemplate();

        original.serialize(queue);
        KeyframeNodeTemplate deserialized = KeyframeNodeTemplate.deserializeGeneral(queue, context);

        assertEquals(original, deserialized);
    }

    @Test
    public void keyframeLayerTemplate()
    {
        ISerialContext context = createMockSerialContext();
        LayerTemplate original = createMockLayerTemplate();

        original.serialize(queue);
        LayerTemplate deserialized = LayerTemplate.deserializeGeneral(queue, context);

        assertEquals(original, deserialized);
    }

    @Test
    public void animator()
    {
        AnimatorTemplate animatorTemplate = createMockAnimatorTemplate();

        animatorTemplate.serialize(queue);

        ISerialContext context = createMockSerialContext();
        AnimatorTemplate deserializedAnimatorTemplate = AnimatorTemplate.deserialize(queue, context);

        assertEquals(animatorTemplate, deserializedAnimatorTemplate);
    }
}
