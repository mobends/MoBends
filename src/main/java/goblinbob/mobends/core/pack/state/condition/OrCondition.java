package goblinbob.mobends.core.pack.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.pack.state.template.MalformedPackTemplateException;
import goblinbob.mobends.core.pack.state.template.TriggerConditionTemplate;

import java.util.LinkedList;
import java.util.List;

public class OrCondition implements ITriggerCondition
{

    private List<ITriggerCondition> conditions;

    public OrCondition(Template template) throws MalformedPackTemplateException
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
    public boolean isConditionMet(EntityData<?> entityData)
    {
        for (ITriggerCondition condition : this.conditions)
        {
            if (condition.isConditionMet(entityData))
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
