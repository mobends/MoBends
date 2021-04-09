package goblinbob.mobends.core.kumo.trigger;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;

/**
 * This condition is met if the nested condition is not met.
 * @author Iwo Plaza
 */
public class NotCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    private ITriggerCondition<D> condition;

    public NotCondition(IKumoInstancingContext<D> context, Template template)
    {
        this.condition = template.condition.instantiate(context);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
    {
        return !this.condition.isConditionMet(context);
    }

    public static class Template extends TriggerConditionTemplate
    {
        public final TriggerConditionTemplate condition;

        public Template(String type, TriggerConditionTemplate condition)
        {
            super(type);
            this.condition = condition;
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new NotCondition<>(context, this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            condition.serialize(out);
        }

        public static <D extends IEntityData, C extends ISerialContext<C, D>> Template deserialize(C context, String type, ISerialInput in) throws IOException
        {
            return new Template(
                    type,
                    TriggerConditionTemplate.deserializeGeneral(context, in)
            );
        }
    }
}
