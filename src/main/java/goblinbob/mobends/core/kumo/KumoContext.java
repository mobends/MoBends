package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

/**
 * A simple implementation of the KUMO context.
 *
 * @author Iwo Plaza
 */
public class KumoContext<D extends IEntityData> implements IKumoContext<D>
{
    public float ticksPassed;

    public D entityData;

    public ILayerState<D> layerState;

    public INodeState<D> currentNode;

    @Override
    public float getTicksPassed()
    {
        return ticksPassed;
    }

    @Override
    public D getEntityData()
    {
        return entityData;
    }

    @Override
    public ILayerState<D> getLayerState()
    {
        return layerState;
    }

    @Override
    public INodeState<D> getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public void setCurrentNode(INodeState<D> node)
    {
        currentNode = node;
    }
}
