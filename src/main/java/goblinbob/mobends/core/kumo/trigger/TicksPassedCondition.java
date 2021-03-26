package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;

import java.io.IOException;

/**
 * This condition is met once a certain amount of ticks have passed since the
 * start of the node's lifetime.
 *
 * @author Iwo Plaza
 */
public class TicksPassedCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    private final float ticksToPass;
    private float ticksOnStart;

    public TicksPassedCondition(IKumoInstancingContext<D> context, Template template)
    {
        this.ticksToPass = template.ticksToPass;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext<D> context)
    {
        this.ticksOnStart = context.getTicksPassed();
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
    {
        return context.getTicksPassed() > this.ticksOnStart + this.ticksToPass;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public int ticksToPass;

        public Template(String type)
        {
            super(type);
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new TicksPassedCondition<>(context, this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeInt(ticksToPass);
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
        {
            Template template = new Template(type);

            template.ticksToPass = in.readInt();

            return template;
        }
    }
}
