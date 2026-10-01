package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Map;

/** A way out of a node, or out of any node inside a machine, that fires when its condition is met. */
public class ConnectionState implements ITransition
{

    public final MachineMember target;
    public final ITriggerCondition triggerCondition;
    public final float transitionDuration;
    public final ConnectionTemplate.Easing transitionEasing;
    /** Layer variables assigned when the connection fires; may be null. */
    public final Map<String, Float> set;

    public ConnectionState(MachineMember target, ITriggerCondition triggerCondition, float transitionDuration, ConnectionTemplate.Easing transitionEasing, Map<String, Float> set)
    {
        this.target = target;
        this.triggerCondition = triggerCondition;
        this.transitionDuration = transitionDuration;
        this.transitionEasing = transitionEasing;
        this.set = set;
    }

    public static ConnectionState createFromTemplate(Map<String, MachineMember> membersByName, ConnectionTemplate template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.target == null)
        {
            throw new MalformedKumoTemplateException("A connection has no target.");
        }
        MachineMember target = membersByName.get(template.target);
        if (target == null)
        {
            throw new MalformedKumoTemplateException(String.format("A connection leads to '%s', which is no node or machine of the layer.", template.target));
        }

        if (template.triggerCondition == null)
        {
            throw new MalformedKumoTemplateException(String.format("The connection to '%s' has no trigger condition.", template.target));
        }

        return new ConnectionState(target,
                TriggerConditionRegistry.INSTANCE.createFromTemplate(template.triggerCondition, scope),
                template.transitionDuration,
                template.transitionEasing == null ? ConnectionTemplate.Easing.EASE_IN_OUT : template.transitionEasing,
                template.set);
    }

    @Override
    public MachineMember getTarget()
    {
        return target;
    }

    @Override
    public float getDuration()
    {
        return transitionDuration;
    }

    @Override
    public ConnectionTemplate.Easing getEasing()
    {
        return transitionEasing;
    }

    @Override
    public Map<String, Float> getSet()
    {
        return set;
    }

}
