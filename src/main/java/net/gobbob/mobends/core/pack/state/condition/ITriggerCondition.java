package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.data.EntityData;

@FunctionalInterface
public interface ITriggerCondition
{

    boolean isConditionMet(EntityData<?> entityData);

}
