package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialInput;

@FunctionalInterface
public interface ILayerTemplateDeserializer
{
    LayerTemplate deserializeLayerTemplate(ISerialInput in);
}
