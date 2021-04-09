package goblinbob.mobends.core.kumo.driver.instruction;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

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

    public static <D extends IEntityData, C extends ISerialContext<C, D>> InstructionTemplate deserializeGeneral(C context, ISerialInput in) throws IOException
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
