package goblinbob.mobends.core.pack.state.condition;

import goblinbob.mobends.core.data.EntityData;

@FunctionalInterface
public interface ITriggerCondition
{

    boolean isConditionMet(EntityData<?> entityData);

}
