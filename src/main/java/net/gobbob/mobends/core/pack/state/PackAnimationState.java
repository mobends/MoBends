package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.BendsTargetTemplate;
import net.gobbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import net.gobbob.mobends.core.pack.state.template.NodeTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PackAnimationState
{

    private BendsPackData bendsPackData;
    private final List<INodeState> nodeStates = new ArrayList<>();
    private INodeState currentNode;

    private void initFor(BendsPackData data, String animatedEntityKey) throws MalformedPackTemplateException
    {
        bendsPackData = data;
        nodeStates.clear();

        if (data.targets == null)
        {
            throw new MalformedPackTemplateException("No targets were specified!");
        }

        BendsTargetTemplate targetTemplate = data.targets.get(animatedEntityKey);
        if (targetTemplate == null)
        {
            return;
        }

        if (targetTemplate.nodes == null)
        {
            throw new MalformedPackTemplateException(String.format("No nodes were specified for '%s'", animatedEntityKey));
        }

        for (NodeTemplate template : targetTemplate.nodes)
        {
            nodeStates.add(new NodeState(data, template));
        }

        for (int i = 0; i < this.nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, targetTemplate.nodes.get(i));
        }

        try
        {
            currentNode = nodeStates.get(targetTemplate.entryNode);
        }
        catch(IndexOutOfBoundsException ex)
        {
            throw new MalformedPackTemplateException("Entry node index is out of bounds");
        }
    }

    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Nullable
    public INodeState update(EntityData<?> entityData, BendsPackData data, String animatedEntityKey, float deltaTime)
    {
        if (bendsPackData != data)
        {
            bendsPackData = data;
            try
            {
                initFor(data, animatedEntityKey);
            }
            catch (MalformedPackTemplateException ex)
            {
                ex.printStackTrace();
            }
        }

        if (currentNode == null)
        {
            return null;
        }

        for (ConnectionState connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(entityData))
            {
                currentNode = connection.targetNode;
                currentNode.start();
                break;
            }
        }

        for (ILayerState layer : currentNode.getLayers())
        {
            layer.update(deltaTime);
        }

        return currentNode;
    }

}
