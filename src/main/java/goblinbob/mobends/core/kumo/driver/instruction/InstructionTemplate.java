package goblinbob.mobends.core.kumo.driver.instruction;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;

public abstract class InstructionTemplate implements ISerializable
{
    private InstructionType type;

    public abstract <D extends IEntityData> IInstruction<D> instantiate(IKumoInstancingContext<D> context);

    @Override
    public void serialize(ISerialOutput out)
    {
        SerialHelper.serializeEnum(this.type, out);
    }

    public static <D extends IEntityData> InstructionTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        InstructionType instructionType = SerialHelper.deserializeEnum(InstructionType.values(), in);

        if (instructionType == null)
        {
            throw new MalformedKumoTemplateException("Unknown instruction type");
        }

        switch (instructionType)
        {
            case SET_POSITION:
                return SetPositionInstructionTemplate.deserialize(context, in);
            default:
                throw new MalformedKumoTemplateException("Unknown expression type");
        }
    }
}
