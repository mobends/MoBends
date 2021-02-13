package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

/**
 * A simple implementation of the KUMO context.
 *
 * @author Iwo Plaza
 */
public class KumoContext implements IKumoContext
{

    public float ticksPassed;

    public IEntityData entityData;

    public ILayerState layerState;

    public INodeState currentNode;

    @Override
    public float getTicksPassed()
    {
        return ticksPassed;
    }

    @Override
    public IEntityData getEntityData()
    {
        return entityData;
    }

    @Override
    public ILayerState getLayerState()
    {
        return layerState;
    }

    @Override
    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public void setCurrentNode(INodeState node)
    {
        currentNode = node;
    }

}
