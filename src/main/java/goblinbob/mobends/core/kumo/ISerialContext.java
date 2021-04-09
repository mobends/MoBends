package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;

public interface ISerialContext<C extends ISerialContext<C, D>, D extends IEntityData>
{
    ISubTypeDeserializer<LayerTemplate, C> getLayerDeserializer();
    ISubTypeDeserializer<KeyframeNodeTemplate, C> getKeyframeNodeDeserializer();
    ISubTypeDeserializer<DriverNodeTemplate, C> getDriverNodeDeserializer();
    ISubTypeDeserializer<TriggerConditionTemplate, C> getTriggerConditionDeserializer();
}
