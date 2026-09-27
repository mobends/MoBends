package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * An edge trigger: met on the frame an expression is lower than on the previous evaluation
 * (e.g. {@code ticksAfterAttack} resetting to 0 on a new attack):
 * {@code {"type": "core:decreased", "value": "ticksAfterAttack"}}.
 *
 * @author Iwo Plaza
 */
public class DecreasedCondition implements ITriggerCondition
{

    private final Expression value;
    private float last;
    private boolean primed;

    public DecreasedCondition(Template template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.value == null)
        {
            throw new MalformedKumoTemplateException("core:decreased needs a 'value'.");
        }
        this.value = Expression.compile(template.value.json, scope);
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        last = value.get(context);
        primed = true;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        float current = value.get(context);
        boolean decreased = primed && current < last;
        last = current;
        primed = true;
        return decreased;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public ExpressionTemplate value;

    }

}
