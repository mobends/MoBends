package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionState;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.NodeState;
import goblinbob.mobends.core.kumo.driver.instruction.IInstruction;
import goblinbob.mobends.core.kumo.driver.instruction.InstructionTemplate;

import java.util.ArrayList;
import java.util.List;

public class StandardDriverNode<D extends IEntityData> extends NodeState<D> implements IDriverNodeState<D>
{
    private final List<IInstruction<D>> instructions = new ArrayList<>();

    public StandardDriverNode(IKumoInstancingContext<D> context, StandardDriverNodeTemplate nodeTemplate)
    {
        this(context, nodeTemplate.instructions);
    }

    public StandardDriverNode(IKumoInstancingContext<D> context, List<InstructionTemplate> instructionTemplates)
    {
        for (InstructionTemplate temp : instructionTemplates)
        {
            this.instructions.add(temp.instantiate(context));
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        for (ConnectionState<D> connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public void update(IKumoContext<D> context)
    {
    }

    @Override
    public void applyTransform(IKumoContext<D> context)
    {
        for (IInstruction<D> instruction : instructions)
        {
            instruction.perform(context);
        }
    }
}
