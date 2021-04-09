package goblinbob.mobends.core.kumo.trigger;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This condition is met if either of the nested conditions are met.
 *
 * @author Iwo Plaza
 */
public class OrCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    private List<ITriggerCondition<D>> conditions;

    public OrCondition(IKumoInstancingContext<D> context, Template template)
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

        public Template(String type)
        {
            super(type);
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new OrCondition<>(context, this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            SerialHelper.serializeList(conditions, out);
        }

        public static <D extends IEntityData, C extends ISerialContext<C, D>> Template deserialize(C context, String type, ISerialInput in) throws IOException
        {
            Template template = new Template(type);

            template.conditions = SerialHelper.deserializeList(context, TriggerConditionTemplate::deserializeGeneral, in);

            return template;
        }
    }
}
