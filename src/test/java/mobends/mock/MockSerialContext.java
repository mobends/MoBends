package mobends.mock;

import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.bendslib.serial.SubTypeRegistry;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.forge.EntityData;

public class MockSerialContext implements ISerialContext<MockSerialContext, EntityData>
{
    public final SubTypeRegistry<LayerTemplate, MockSerialContext> layerRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<KeyframeNodeTemplate, MockSerialContext> keyframeNodeRegistry = new SubTypeRegistry<>();

    @Override
    public ISubTypeDeserializer<LayerTemplate, MockSerialContext> getLayerDeserializer()
    {
        return layerRegistry;
    }

    @Override
    public ISubTypeDeserializer<KeyframeNodeTemplate, MockSerialContext> getKeyframeNodeDeserializer()
    {
        return keyframeNodeRegistry;
    }

    @Override
    public ISubTypeDeserializer<DriverNodeTemplate, MockSerialContext> getDriverNodeDeserializer()
    {
        return null;
    }

    @Override
    public ISubTypeDeserializer<TriggerConditionTemplate, MockSerialContext> getTriggerConditionDeserializer()
    {
        return null;
    }
}
