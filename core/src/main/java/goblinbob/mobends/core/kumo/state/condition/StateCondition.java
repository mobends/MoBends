package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * This condition is met once the subject is in the provided state (e.g. ON_GROUND, AIRBORNE).
 * States are looked up by name on the subject, so entity data classes and addons can add their
 * own without touching this class; an unknown one fails the animator.
 *
 * @author Iwo Plaza
 */
public class StateCondition implements ITriggerCondition
{

    private final VariableTable.State state;

    public StateCondition(Template template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        if (template.state == null)
        {
            throw new MalformedKumoTemplateException("No 'state' property given for trigger condition.");
        }

        this.state = scope.getVariables().state(template.state);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        return state.get(context.getSubject());
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String state;

    }

}
