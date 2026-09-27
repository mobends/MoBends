package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * This condition is met once a certain amount of ticks have passed since the
 * start of the node's lifetime. Measured on the layer's own clock, so it is unaffected by
 * world changes resetting the global tick counter.
 *
 * @author Iwo Plaza
 */
public class TicksPassedCondition implements ITriggerCondition
{

    private final float ticksToPass;
    private float ticksOnStart;

    public TicksPassedCondition(Template template)
    {
        this.ticksToPass = template.ticksToPass;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        this.ticksOnStart = context.getLayerState().getElapsedTicks();
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        return context.getLayerState().getElapsedTicks() > this.ticksOnStart + this.ticksToPass;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public float ticksToPass;

    }

}
