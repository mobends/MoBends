package mobends.mock;

import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.serial.SubTypeRegistry;

public class MockSerialContext implements ISerialContext
{
    public final SubTypeRegistry<KeyframeNodeTemplate> keyframeNodeRegistry = new SubTypeRegistry<KeyframeNodeTemplate>();

    @Override
    public ISubTypeDeserializer<KeyframeNodeTemplate> getKeyframeNodeDeserializer()
    {
        return keyframeNodeRegistry;
    }
}
