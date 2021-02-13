package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerializable;

import java.io.IOException;

public abstract class LayerTemplate<D extends IEntityData> implements ISerializable
{
    public abstract ILayerState<D> produceInstance(IKumoInstancingContext<D> context) throws MalformedKumoTemplateException;

    public static LayerTemplate<?> deserializeGeneral(ISerialInput in, ISerialContext context) throws IOException
    {
        LayerType layerType = LayerType.values()[in.readByte()];
        return layerType.getDeserializer().deserialize(in, context);
    }
}
