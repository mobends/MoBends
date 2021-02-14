package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

public interface ISerialContext<D extends IEntityData>
{
    ISubTypeDeserializer<LayerTemplate, D> getLayerDeserializer();
    ISubTypeDeserializer<KeyframeNodeTemplate, D> getKeyframeNodeDeserializer();
    ISubTypeDeserializer<TriggerConditionTemplate, D> getTriggerConditionDeserializer();
}
