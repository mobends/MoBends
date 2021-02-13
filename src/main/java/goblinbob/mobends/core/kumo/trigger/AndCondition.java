package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.TriggerConditionTemplate;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * This condition is met if either of the nested conditions are met.
 *
 * @author Iwo Plaza
 */
public class AndCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    private List<ITriggerCondition<D>> conditions;

    public AndCondition(Template template, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException
    {
        this.conditions = new LinkedList<>();
        for (TriggerConditionTemplate conditionTemplate : template.conditions)
        {
            if (conditionTemplate != null)
            {
                this.conditions.add(context.createTriggerCondition(conditionTemplate, context));
            }
        }
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context) throws MalformedKumoTemplateException
    {
        for (ITriggerCondition<D> condition : this.conditions)
        {
            if (!condition.isConditionMet(context))
            {
                return false;
            }
        }
        return true;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public List<TriggerConditionTemplate> conditions;

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);
            SerialHelper.serializeList(conditions, out);
        }
    }
}
