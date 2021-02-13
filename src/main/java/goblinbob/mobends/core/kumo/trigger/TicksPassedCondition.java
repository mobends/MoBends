package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.kumo.TriggerConditionTemplate;
import goblinbob.mobends.forge.EntityData;

/**
 * This condition is met once a certain amount of ticks have passed since the
 * start of the node's lifetime.
 *
 * @author Iwo Plaza
 */
public class TicksPassedCondition implements ITriggerCondition<EntityData>
{
    private final float ticksToPass;
    private float ticksOnStart;

    public TicksPassedCondition(Template template)
    {
        this.ticksToPass = template.ticksToPass;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext<EntityData> context)
    {
        this.ticksOnStart = context.getTicksPassed();
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<EntityData> context)
    {
        return context.getTicksPassed() > this.ticksOnStart + this.ticksToPass;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public int ticksToPass;
    }
}
