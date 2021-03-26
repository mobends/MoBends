package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;

@FunctionalInterface
public interface ITriggerCondition<D extends IEntityData>
{
    default void onNodeStarted(ITriggerConditionContext<D> context) {}

    boolean isConditionMet(ITriggerConditionContext<D> context);
}
