package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.data.IEntityData;

import java.io.IOException;

public abstract class LayerTemplate implements ISerializable
{
    private final String type;

    public LayerTemplate(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public abstract <D extends IEntityData> ILayerState<D> instantiate(IKumoInstancingContext<D> context);

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(type);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> LayerTemplate deserializeGeneral(C context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<LayerTemplate, C> deserializer = context.getLayerDeserializer();
        return deserializer.deserialize(context, type, in);
    }
}
