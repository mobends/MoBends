package mobends.mock;

import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.serial.SubTypeRegistry;
import goblinbob.mobends.forge.EntityData;

public class MockSerialContext implements ISerialContext<EntityData>
{
    public final SubTypeRegistry<LayerTemplate, EntityData> layerRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<KeyframeNodeTemplate, EntityData> keyframeNodeRegistry = new SubTypeRegistry<>();

    @Override
    public ISubTypeDeserializer<LayerTemplate, EntityData> getLayerDeserializer()
    {
        return layerRegistry;
    }

    @Override
    public ISubTypeDeserializer<KeyframeNodeTemplate, EntityData> getKeyframeNodeDeserializer()
    {
        return keyframeNodeRegistry;
    }

    @Override
    public ISubTypeDeserializer<DriverNodeTemplate, EntityData> getDriverNodeDeserializer()
    {
        return null;
    }

    @Override
    public ISubTypeDeserializer<TriggerConditionTemplate, EntityData> getTriggerConditionDeserializer()
    {
        return null;
    }
}
