package net.gobbob.mobends.core.pack.state;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.pack.BendsPackData;
import net.gobbob.mobends.core.pack.state.template.BendsTargetTemplate;
import net.gobbob.mobends.core.pack.state.template.ConnectionTemplate;
import net.gobbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import net.gobbob.mobends.core.pack.state.template.NodeTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PackAnimationState
{

    private BendsPackData bendsPackData;
    private final List<INodeState> nodeStates = new ArrayList<>();
    private final List<ConnectionState> entryConnections = new ArrayList<>();
    private INodeState currentNode = null;

    private void initFor(BendsPackData data, String animatedEntityKey) throws MalformedPackTemplateException
    {
        this.bendsPackData = data;

        this.nodeStates.clear();
        this.entryConnections.clear();
        this.currentNode = null;

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
            this.nodeStates.add(new NodeState(data, template));
        }

        for (int i = 0; i < this.nodeStates.size(); ++i)
        {
            this.nodeStates.get(i).parseConnections(this.nodeStates, targetTemplate.nodes.get(i));
        }

        for (ConnectionTemplate template : targetTemplate.entryConnections)
        {
            this.entryConnections.add(ConnectionState.createFromTemplate(this.nodeStates, template));
        }
    }

    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Nullable
    public INodeState update(EntityData<?> entityData, BendsPackData data, String animatedEntityKey, float deltaTime)
    {
        if (this.bendsPackData != data)
        {
            this.bendsPackData = data;
            try
            {
                this.nodeStates.clear();
                this.entryConnections.clear();
                this.initFor(data, animatedEntityKey);
            }
            catch (MalformedPackTemplateException ex)
            {
                ex.printStackTrace();
            }
        }

        if (this.currentNode == null)
        {
            for (ConnectionState connection : this.entryConnections)
            {
                if (connection.triggerCondition.isConditionMet(entityData))
                {
                    this.currentNode = connection.targetNode;
                    if (this.currentNode != null)
                        this.currentNode.start();
                    break;
                }
            }
            return null;
        }

        for (ConnectionState connection : this.currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(entityData))
            {
                this.currentNode = connection.targetNode;
                if (this.currentNode != null)
                    this.currentNode.start();
                break;
            }
        }

        if (this.currentNode != null)
        {
            for (ILayerState layer : this.currentNode.getLayers())
            {
                layer.update(deltaTime);
            }
        }

        return this.currentNode;
    }

}
