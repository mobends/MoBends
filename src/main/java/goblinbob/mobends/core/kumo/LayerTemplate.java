package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

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

    public static <D extends IEntityData> LayerTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<LayerTemplate, D> deserializer = context.getLayerDeserializer();
        return deserializer.deserialize(context, type, in);
    }
}
