package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

public class StateCondition implements ITriggerCondition
{

    private final State state;

    public StateCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.state == null)
        {
            throw new MalformedKumoTemplateException("No 'state' property given for trigger condition.");
        }

        this.state = template.state;
    }

    @Override
    public boolean isConditionMet(EntityData<?> entityData)
    {
        switch (this.state)
        {
            case ON_GROUND:
                return entityData.isOnGround();
            case AIRBORNE:
                return !entityData.isOnGround();
            case SPRINTING:
                return entityData.getEntity().isSprinting();
            case STANDING_STILL:
                return entityData.isStillHorizontally();
            case MOVING_HORIZONTALLY:
                return !entityData.isStillHorizontally();
            default:
                return false;
        }
    }

    public static class Template extends TriggerConditionTemplate
    {

        public State state;

    }

    public enum State
    {
        ON_GROUND,
        AIRBORNE,
        SPRINTING,
        STANDING_STILL,
        MOVING_HORIZONTALLY,
    }


}
