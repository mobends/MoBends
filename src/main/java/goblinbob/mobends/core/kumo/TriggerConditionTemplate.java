package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;

public abstract class TriggerConditionTemplate implements ISerializable
{
    private String type;

    public String getType()
    {
        return type;
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(type);
    }
}
