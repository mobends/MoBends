package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 * This condition is met if either of the nested conditions are met.
 *
 * @author Iwo Plaza
 */
public class AndCondition implements ITriggerCondition
{

    private List<ITriggerCondition> conditions;

    public AndCondition(Template template) throws MalformedKumoTemplateException
    {
        this.conditions = new LinkedList<>();
        for (TriggerConditionTemplate conditionTemplate : template.conditions)
        {
            if (conditionTemplate != null)
            {
                this.conditions.add(TriggerConditionRegistry.instance.createFromTemplate(conditionTemplate));
            }
        }
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        // No short-circuit: nested edge triggers (core:decreased) must see every frame.
        boolean met = true;
        for (ITriggerCondition condition : this.conditions)
        {
            if (!condition.isConditionMet(context))
            {
                met = false;
            }
        }
        return met;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        for (ITriggerCondition condition : this.conditions)
        {
            condition.onNodeStarted(context);
        }
    }

    public static class Template extends TriggerConditionTemplate
    {

        public List<TriggerConditionTemplate> conditions;

    }

}
