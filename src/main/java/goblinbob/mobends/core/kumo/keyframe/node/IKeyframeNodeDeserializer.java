package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.serial.ISerialInput;

public interface IKeyframeNodeDeserializer
{
    KeyframeNodeTemplate deserialize(String type, ISerialInput in);
}
