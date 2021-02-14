package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

import java.io.IOException;

public abstract class TriggerConditionTemplate implements ISerializable
{
    private final String type;

    public TriggerConditionTemplate(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(type);
    }

    public abstract <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context);

    public static <D extends IEntityData> TriggerConditionTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<TriggerConditionTemplate, D> deserializer = context.getTriggerConditionDeserializer();
        return deserializer.deserialize(context, type, in);
    }
}
