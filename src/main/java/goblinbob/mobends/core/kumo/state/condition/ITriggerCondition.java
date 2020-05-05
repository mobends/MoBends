package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;

@FunctionalInterface
public interface ITriggerCondition
{

    boolean isConditionMet(EntityData<?> entityData);

}
