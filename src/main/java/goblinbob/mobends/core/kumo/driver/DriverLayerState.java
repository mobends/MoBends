package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.kumo.driver.node.IDriverNodeState;

import java.util.ArrayList;
import java.util.List;

public class DriverLayerState<D extends IEntityData> implements ILayerState<D>
{
    private List<IDriverNodeState<D>> nodeStates = new ArrayList<>();
    private IDriverNodeState<D> previousNode;
    private IDriverNodeState<D> currentNode;
    private float transitionStartTime = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;

    public DriverLayerState(DriverLayerTemplate layerTemplate, IKumoInstancingContext<D> context)
    {
        try
        {
            for (DriverNodeTemplate nodeTemplate : layerTemplate.nodes)
            {
                nodeStates.add((IDriverNodeState<D>) nodeTemplate.instantiate(context));
            }
        }
        catch(ClassCastException e)
        {
            throw new AnimationRuntimeException("Cannot use a non-DriverNode in a DriverLayer.");
        }

        for (int i = 0; i < nodeStates.size(); ++i)
        {
            nodeStates.get(i).parseConnections(nodeStates, layerTemplate.nodes[i], context);
        }

        try
        {
            currentNode = nodeStates.get(layerTemplate.entryNode);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new AnimationRuntimeException("Entry node index is out of bounds.");
        }
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        currentNode.start(context);
    }

    @Override
    public void update(IKumoContext<D> context)
    {
        if (currentNode != null)
        {
            currentNode.applyTransform(context);
        }

        // Populating the context.
        context.setCurrentNode(currentNode);

        // Updating node states.
        for (INodeState<D> node : nodeStates)
        {
            node.update(context);
        }

        // Evaluating connection trigger conditions.
        for (ConnectionState<D> connection : currentNode.getConnections())
        {
            if (connection.triggerCondition.isConditionMet(context))
            {
                // Transition setup
                transitionDuration = connection.transitionDuration;
                transitionEasing = connection.transitionEasing;
                if (transitionDuration == 0.0F)
                {
                    previousNode = null;
                }
                else
                {
                    previousNode = currentNode;
                    transitionStartTime = context.getTicksPassed();
                }

                currentNode = (IDriverNodeState<D>) connection.targetNode;
                currentNode.start(context);

                break;
            }
        }
    }
}
