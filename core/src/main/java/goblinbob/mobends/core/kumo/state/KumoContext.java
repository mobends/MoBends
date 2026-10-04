package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;


/**
 * The KUMO context of one animator: the subject, the frame (its number and delta time), and the
 * layer and node being evaluated.
 *
 * @author Iwo Plaza
 */
public class KumoContext implements IKumoContext
{

    private IKumoSubject subject;
    private float deltaTime;
    private LayerState layerState;
    private INodeState currentNode;
    /** Counts the frames, so a live definition is computed once in each. */
    private long frame;

    /** Starts a frame of {@code subject}. */
    public void beginFrame(IKumoSubject subject, float deltaTime)
    {
        this.subject = subject;
        this.deltaTime = deltaTime;
        this.frame++;
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
    public long getFrame()
    {
        return frame;
    }

    @Override
    public void enterNode(INodeState node)
    {
        this.currentNode = node;
    }

    @Override
    public double resolveVariable(VariableTable.Read read)
    {
        return read.get(subject);
    }

}
