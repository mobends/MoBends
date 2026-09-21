package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;

import java.util.List;
import java.util.Map;

public class ConnectionState
{

    public final INodeState targetNode;
    public final ITriggerCondition triggerCondition;
    public final float transitionDuration;
    public ConnectionTemplate.Easing transitionEasing;

    public ConnectionState(INodeState targetNode, ITriggerCondition triggerCondition, float transitionDuration, ConnectionTemplate.Easing transitionEasing)
    {
        this.targetNode = targetNode;
        this.triggerCondition = triggerCondition;
        this.transitionDuration = transitionDuration;
        this.transitionEasing = transitionEasing;
    }

    public static ConnectionState createFromTemplate(List<INodeState> nodes, Map<String, INodeState> nodesByName, ConnectionTemplate template) throws MalformedKumoTemplateException
    {
        INodeState node;

        if (template.target != null)
        {
            node = nodesByName.get(template.target);
            if (node == null)
            {
                throw new MalformedKumoTemplateException(String.format("A connection to node '%s' was specified, which doesn't exist.", template.target));
            }
        }
        else
        {
            if (template.targetNodeIndex < 0 || template.targetNodeIndex >= nodes.size())
            {
                throw new MalformedKumoTemplateException(String.format("A connection to node at index: %d was specified, which doesn't exist.",
                        template.targetNodeIndex));
            }
            node = nodes.get(template.targetNodeIndex);
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedKumoTemplateException("No trigger condition was specified for a connection.");
        }

        return new ConnectionState(node,
                TriggerConditionRegistry.instance.createFromTemplate(template.triggerCondition),
                template.transitionDuration,
                template.transitionEasing == null ? ConnectionTemplate.Easing.EASE_IN_OUT : template.transitionEasing);
    }

}
