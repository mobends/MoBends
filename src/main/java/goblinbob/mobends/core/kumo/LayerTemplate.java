package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerializable;

import java.io.IOException;

public abstract class LayerTemplate implements ISerializable
{
    public static LayerTemplate deserializeGeneral(ISerialInput in, ISerialContext context) throws IOException
    {
        LayerType layerType = LayerType.values()[in.readByte()];
        return layerType.getDeserializer().deserialize(in, context);
    }
}
