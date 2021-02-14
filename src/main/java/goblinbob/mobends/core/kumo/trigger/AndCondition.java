package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
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

    public AndCondition(IKumoInstancingContext<D> context, Template template)
    {
        this.conditions = new LinkedList<>();
        for (TriggerConditionTemplate conditionTemplate : template.conditions)
        {
            if (conditionTemplate != null)
            {
                this.conditions.add(conditionTemplate.instantiate(context));
            }
        }
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
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

        public Template(String type)
        {
            super(type);
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new AndCondition<D>(context, this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);
            SerialHelper.serializeList(conditions, out);
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
        {
            Template template = new Template(type);

            template.conditions = SerialHelper.deserializeList(context, TriggerConditionTemplate::deserializeGeneral, in);

            return template;
        }
    }
}
