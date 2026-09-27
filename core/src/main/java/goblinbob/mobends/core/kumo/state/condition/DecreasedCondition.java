package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * An edge trigger: met on the frame a variable is lower than on the previous evaluation
 * (e.g. {@code ticksAfterAttack} resetting to 0 on a new attack).
 *
 * @author Iwo Plaza
 */
public class DecreasedCondition implements ITriggerCondition
{

    private final String variable;
    private double last;
    private boolean primed;

    public DecreasedCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.variable == null)
        {
            throw new MalformedKumoTemplateException("core:decreased needs a 'variable'.");
        }
        this.variable = template.variable;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        last = context.resolveVariable(variable);
        primed = true;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        double current = context.resolveVariable(variable);
        boolean decreased = primed && current < last;
        last = current;
        primed = true;
        return decreased;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String variable;

    }

}
