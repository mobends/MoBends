package goblinbob.mobends.forge;

import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.serial.SubTypeRegistry;

public class SerialContext implements ISerialContext<EntityData>
{
    public final SubTypeRegistry<LayerTemplate, EntityData> layerRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<KeyframeNodeTemplate, EntityData> keyframeNodeRegistry = new SubTypeRegistry<>();
    public final SubTypeRegistry<TriggerConditionTemplate, EntityData> triggerConditionRegistry = new SubTypeRegistry<>();

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
    public ISubTypeDeserializer<TriggerConditionTemplate, EntityData> getTriggerConditionDeserializer()
    {
        return triggerConditionRegistry;
    }
}
