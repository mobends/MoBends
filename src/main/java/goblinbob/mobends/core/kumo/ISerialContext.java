package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

public interface ISerialContext
{
    ISubTypeDeserializer<KeyframeNodeTemplate> getKeyframeNodeDeserializer();
}
