package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.serial.IDeserializer;

public enum LayerType
{
    KEYFRAME(KeyframeLayerTemplate::deserialize);

    private IContexedDeserializer<LayerTemplate> deserializer;

    LayerType(IContexedDeserializer<LayerTemplate> deserializer)
    {
        this.deserializer = deserializer;
    }

    public IContexedDeserializer<LayerTemplate> getDeserializer()
    {
        return this.deserializer;
    }
}
