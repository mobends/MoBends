package goblinbob.mobends.core.kumo.trigger;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

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

    public static <D extends IEntityData, C extends ISerialContext<C, D>> TriggerConditionTemplate deserializeGeneral(C context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<TriggerConditionTemplate, C> deserializer = context.getTriggerConditionDeserializer();
        return deserializer.deserialize(context, type, in);
    }
}
