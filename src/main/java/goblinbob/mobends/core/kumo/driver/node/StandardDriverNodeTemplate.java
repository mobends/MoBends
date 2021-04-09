package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.driver.instruction.InstructionTemplate;

import java.io.IOException;
import java.util.List;

public class StandardDriverNodeTemplate extends DriverNodeTemplate
{
    public final List<InstructionTemplate> instructions;

    public StandardDriverNodeTemplate(String type, List<ConnectionTemplate> connections, List<InstructionTemplate> instructions)
    {
        super(type, connections);

        this.instructions = instructions;
    }

    @Override
    public <D extends IEntityData> INodeState<D> instantiate(IKumoInstancingContext<D> context)
    {
        return new StandardDriverNode<>(context, this);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> StandardDriverNodeTemplate deserialize(C context, String type, ISerialInput in) throws IOException
    {
        List<ConnectionTemplate> connections = SerialHelper.deserializeList(context, ConnectionTemplate::deserialize, in);

        List<InstructionTemplate> instructions = SerialHelper.deserializeList(context, InstructionTemplate::deserializeGeneral, in);

        return new StandardDriverNodeTemplate(type, connections, instructions);
    }
}
