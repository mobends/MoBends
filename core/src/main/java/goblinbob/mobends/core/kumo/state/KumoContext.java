package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;

import java.util.Collections;
import java.util.List;

/**
 * The KUMO context of one animator: the subject, the frame's delta time, the layer and node
 * being evaluated and their variable scopes.
 *
 * @author Iwo Plaza
 */
public class KumoContext implements IKumoContext
{

    private IKumoSubject subject;
    private float deltaTime;
    /** The animator's layers, for {@link #isActionActive}. */
    private List<LayerState> layers = Collections.emptyList();
    private LayerState layerState;
    private INodeState currentNode;
    private VariableScope layerScope = new VariableScope();
    private VariableScope nodeScope = new VariableScope();

    public void setLayers(List<LayerState> layers)
    {
        this.layers = layers;
    }

    /** Starts a frame of {@code subject}. */
    public void beginFrame(IKumoSubject subject, float deltaTime)
    {
        this.subject = subject;
        this.deltaTime = deltaTime;
    }

    public void setLayerState(LayerState layerState)
    {
        this.layerState = layerState;
    }

    @Override
    public IKumoSubject getSubject()
    {
        return subject;
    }

    @Override
    public LayerState getLayerState()
    {
        return layerState;
    }

    @Override
    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public float getDeltaTime()
    {
        return deltaTime;
    }

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
    public void enterNode(INodeState node, VariableScope layerScope)
    {
        this.currentNode = node;
        this.layerScope = layerScope;
        this.nodeScope = node.getScope();
    }

    @Override
    public double resolveVariable(VariableTable.Read read)
    {
        return read.get(nodeScope, layerScope, subject);
    }

    @Override
    public boolean isActionActive(String tag)
    {
        for (LayerState layer : layers)
        {
            if (layer.getActions().contains(tag))
            {
                return true;
            }
        }
        return false;
    }

}
