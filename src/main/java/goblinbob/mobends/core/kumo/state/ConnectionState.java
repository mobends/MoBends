package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;

import java.util.List;

public class ConnectionState
{

    public final INodeState targetNode;
    public final ITriggerCondition triggerCondition;
    public final float transitionDuration;

    public ConnectionState(INodeState targetNode, ITriggerCondition triggerCondition, float transitionDuration)
    {
        this.targetNode = targetNode;
        this.triggerCondition = triggerCondition;
        this.transitionDuration = transitionDuration;
    }

    public static ConnectionState createFromTemplate(List<INodeState> nodes, ConnectionTemplate template) throws MalformedKumoTemplateException
    {
        INodeState node = null;

        try
        {
            node = nodes.get(template.targetNodeIndex);
        }
        catch (IndexOutOfBoundsException ex)
        {
            throw new MalformedKumoTemplateException(String.format("A connection to node at index: %d was specified, which doesn't exist.",
                    template.targetNodeIndex));
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedKumoTemplateException("No trigger condition was specified for a connection.");
        }

        return new ConnectionState(node, TriggerConditionRegistry.instance.createFromTemplate(template.triggerCondition), template.transitionDuration);
    }

}
