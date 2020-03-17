package goblinbob.mobends.core.pack.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.pack.state.template.TriggerConditionTemplate;

public class GroundedCondition implements ITriggerCondition
{

    public GroundedCondition(TriggerConditionTemplate template)
    {

    }

    @Override
    public boolean isConditionMet(EntityData<?> entityData)
    {
        return entityData.isOnGround();
    }

}
