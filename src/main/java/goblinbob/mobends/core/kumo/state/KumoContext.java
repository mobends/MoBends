package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;

/**
 * A simple implementation of the KUMO context.
 *
 * @author Iwo Plaza
 */
public class KumoContext implements IKumoContext
{

    public IKumoSubject subject;

    public ILayerState layerState;

    public INodeState currentNode;

    public float deltaTime;

    /** The animator's layers, for {@link #isActionActive}. */
    public java.util.List<ILayerState> layers = java.util.Collections.emptyList();

    @Override
    public IKumoSubject getSubject()
    {
        return subject;
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

    @Override
    public float getDeltaTime()
    {
        return deltaTime;
    }

    @Override
    public boolean isActionActive(String tag)
    {
        for (ILayerState layer : layers)
        {
            if (layer.getActions().contains(tag))
            {
                return true;
            }
        }
        return false;
    }

}
