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

    /** Node-local variables of the node being evaluated (set by the layer before evaluation). */
    public VariableScope nodeScope;

    /** Variables of the layer being evaluated. */
    public VariableScope layerScope;

    @Override
    public VariableScope getNodeScope()
    {
        return nodeScope;
    }

    @Override
    public VariableScope getLayerScope()
    {
        return layerScope;
    }

    @Override
    public double resolveVariable(String name)
    {
        if (nodeScope != null && nodeScope.has(name)) return nodeScope.get(name);
        if (layerScope != null && layerScope.has(name)) return layerScope.get(name);
        return subject.getVariable(name);
    }

    @Override
    public boolean hasVariable(String name)
    {
        return (nodeScope != null && nodeScope.has(name)) || (layerScope != null && layerScope.has(name)) || subject.hasVariable(name);
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
