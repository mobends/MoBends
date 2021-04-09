package goblinbob.mobends.forge;

import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.bendslib.serial.SubTypeRegistry;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;

public class SerialContext implements ISerialContext<SerialContext, EntityData>
{
    public final SubTypeRegistry<LayerTemplate, SerialContext> layerRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<KeyframeNodeTemplate, SerialContext> keyframeNodeRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<DriverNodeTemplate, SerialContext> driverNodeRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<TriggerConditionTemplate, SerialContext> triggerConditionRegistry = new SubTypeRegistry<>();

    @Override
    public ISubTypeDeserializer<LayerTemplate, SerialContext> getLayerDeserializer()
    {
        return layerRegistry;
    }

    @Override
    public ISubTypeDeserializer<KeyframeNodeTemplate, SerialContext> getKeyframeNodeDeserializer()
    {
        return keyframeNodeRegistry;
    }

    @Override
    public ISubTypeDeserializer<DriverNodeTemplate, SerialContext> getDriverNodeDeserializer()
    {
        return driverNodeRegistry;
    }

    @Override
    public ISubTypeDeserializer<TriggerConditionTemplate, SerialContext> getTriggerConditionDeserializer()
    {
        return triggerConditionRegistry;
    }
}
