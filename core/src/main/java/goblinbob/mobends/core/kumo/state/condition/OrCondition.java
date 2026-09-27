package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * This condition is met if any of the nested conditions is met.
 *
 * @author Iwo Plaza
 */
public class OrCondition implements ITriggerCondition
{

    private final List<ITriggerCondition> conditions = new ArrayList<>();

    public OrCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.conditions == null)
        {
            throw new MalformedKumoTemplateException("core:or needs 'conditions'.");
        }
        for (TriggerConditionTemplate conditionTemplate : template.conditions)
        {
            if (conditionTemplate == null)
            {
                throw new MalformedKumoTemplateException("core:or has a null condition.");
            }
            this.conditions.add(TriggerConditionRegistry.INSTANCE.createFromTemplate(conditionTemplate));
        }
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        // No short-circuit: nested edge triggers (core:decreased) must see every frame.
        boolean met = false;
        for (ITriggerCondition condition : this.conditions)
        {
            if (condition.isConditionMet(context))
            {
                met = true;
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
