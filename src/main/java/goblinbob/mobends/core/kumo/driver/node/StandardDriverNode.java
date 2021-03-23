package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.*;

public class StandardDriverNode<D extends IEntityData> extends NodeState<D> implements IDriverNodeState<D>
{
    public StandardDriverNode(IKumoInstancingContext<D> context, StandardDriverNodeTemplate nodeTemplate)
    {
        this();
    }

    public StandardDriverNode()
    {
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
        throw new AnimationRuntimeException("The standard driver node has not been implemented yet.");
    }
}
