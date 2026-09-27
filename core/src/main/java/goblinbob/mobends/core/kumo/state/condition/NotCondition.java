package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * This condition is met if the nested condition is not met.
 * @author Iwo Plaza
 */
public class NotCondition implements ITriggerCondition
{

    private final ITriggerCondition condition;

    public NotCondition(Template template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.condition == null)
        {
            throw new MalformedKumoTemplateException("core:not needs a 'condition'.");
        }
        this.condition = TriggerConditionRegistry.INSTANCE.createFromTemplate(template.condition, scope);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        return !this.condition.isConditionMet(context);
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        this.condition.onNodeStarted(context);
    }

    public static class Template extends TriggerConditionTemplate
    {

        public TriggerConditionTemplate condition;

    }

}
