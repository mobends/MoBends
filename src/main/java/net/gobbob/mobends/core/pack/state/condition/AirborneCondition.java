package net.gobbob.mobends.core.pack.state.condition;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.pack.state.template.TriggerConditionTemplate;

public class AirborneCondition implements ITriggerCondition
{

    public AirborneCondition(TriggerConditionTemplate template)
    {

    }

    @Override
    public boolean isConditionMet(EntityData<?> entityData)
    {
        return !entityData.isOnGround();
    }

}
