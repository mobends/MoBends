package mobends.core.kumo;

import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.StandardKeyframeNodeTemplate;
import goblinbob.mobends.forge.EntityData;
import mobends.mock.MockSerialContext;
import mobends.mock.MockSerialQueue;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KumoAnimatorSerializationTest
{

    ISerialContext<EntityData> createMockSerialContext()
    {
        MockSerialContext context = new MockSerialContext();
        context.keyframeNodeRegistry.register("core:standard", StandardKeyframeNodeTemplate::deserialize);

        return context;
    }

    KeyframeNodeTemplate createMockKeyframeNodeTemplate()
    {
        return new StandardKeyframeNodeTemplate("core:standard", "animation", rand.nextInt(), rand.nextFloat(), rand.nextBoolean());
    }

    LayerTemplate createMockLayerTemplate()
    {
        return new KeyframeLayerTemplate("core:keyframe", 0, null, new KeyframeNodeTemplate[] {
                createMockKeyframeNodeTemplate(),
        });
    }

    AnimatorTemplate createMockAnimatorTemplate()
    {
        AnimatorTemplate animatorTemplate = new AnimatorTemplate();

        StandardKeyframeNodeTemplate nodeOne = new StandardKeyframeNodeTemplate("core:standard", "hello:there", 0, 1, false);

        KeyframeLayerTemplate sampleLayer = new KeyframeLayerTemplate("core:keyframe", 0, null, new KeyframeNodeTemplate[] {
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
        ISerialContext<EntityData> context = createMockSerialContext();
        KeyframeNodeTemplate original = createMockKeyframeNodeTemplate();

        original.serialize(queue);

        try
        {
            KeyframeNodeTemplate deserialized = KeyframeNodeTemplate.deserializeGeneral(context, queue);

            assertEquals(original, deserialized);
        }
        catch (IOException e)
        {
            fail("An IOException has occurred during KeyframeNodeTemplate deserialization.");
        }
    }

    @Test
    public void keyframeLayerTemplate()
    {
        ISerialContext<EntityData> context = createMockSerialContext();
        LayerTemplate original = createMockLayerTemplate();

        original.serialize(queue);

        try
        {
            LayerTemplate deserialized = LayerTemplate.deserializeGeneral(context, queue);

            assertEquals(original, deserialized);
        }
        catch (IOException e)
        {
            fail("An IOException has occurred during LayerTemplate deserialization.");
        }
    }

    @Test
    public void animator()
    {
        AnimatorTemplate animatorTemplate = createMockAnimatorTemplate();

        animatorTemplate.serialize(queue);

        ISerialContext<EntityData> context = createMockSerialContext();

        try
        {
            AnimatorTemplate deserializedAnimatorTemplate = AnimatorTemplate.deserialize(context, queue);

            assertEquals(animatorTemplate, deserializedAnimatorTemplate);
        }
        catch (IOException e)
        {
            fail("An IOException has occurred during AnimatorTemplate deserialization.");
        }
    }
}
