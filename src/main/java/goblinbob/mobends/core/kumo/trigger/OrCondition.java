package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.TriggerConditionTemplate;

import java.util.LinkedList;
import java.util.List;

/**
 * This condition is met if either of the nested conditions are met.
 *
 * @author Iwo Plaza
 */
public class OrCondition implements ITriggerCondition
{

    private List<ITriggerCondition> conditions;

    public OrCondition(Template template) throws MalformedKumoTemplateException
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
        for (ITriggerCondition condition : this.conditions)
        {
            if (condition.isConditionMet(context))
            {
                return true;
            }
        }
        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public List<TriggerConditionTemplate> conditions;

    }

}
