package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;

import java.util.Map;

public class ConnectionState
{

    public final INodeState targetNode;
    public final ITriggerCondition triggerCondition;
    public final float transitionDuration;
    public ConnectionTemplate.Easing transitionEasing;
    /** Layer variables assigned when the connection fires; may be null. */
    public Map<String, Float> set;

    public ConnectionState(INodeState targetNode, ITriggerCondition triggerCondition, float transitionDuration, ConnectionTemplate.Easing transitionEasing)
    {
        this.targetNode = targetNode;
        this.triggerCondition = triggerCondition;
        this.transitionDuration = transitionDuration;
        this.transitionEasing = transitionEasing;
    }

    public static ConnectionState createFromTemplate(Map<String, INodeState> nodesByName, ConnectionTemplate template) throws MalformedKumoTemplateException
    {
        if (template.target == null)
        {
            throw new MalformedKumoTemplateException("A connection has no target.");
        }
        INodeState node = nodesByName.get(template.target);
        if (node == null)
        {
            throw new MalformedKumoTemplateException(String.format("A connection to node '%s' was specified, which doesn't exist.", template.target));
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedKumoTemplateException("No trigger condition was specified for a connection.");
        }

        ConnectionState state = new ConnectionState(node,
                TriggerConditionRegistry.INSTANCE.createFromTemplate(template.triggerCondition),
                template.transitionDuration,
                template.transitionEasing == null ? ConnectionTemplate.Easing.EASE_IN_OUT : template.transitionEasing);
        state.set = template.set;
        return state;
    }

}
