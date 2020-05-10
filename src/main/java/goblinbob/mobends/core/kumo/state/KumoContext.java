package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.data.EntityData;

/**
 * A simple implementation of the KUMO context.
 *
 * @author Iwo Plaza
 */
public class KumoContext implements IKumoContext
{

    public EntityData<?> entityData;

    public ILayerState layerState;

    public INodeState currentNode;

    @Override
    public EntityData<?> getEntityData()
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
