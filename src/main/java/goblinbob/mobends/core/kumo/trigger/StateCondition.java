package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;

import java.io.IOException;

/**
 * This condition is met once the entity is in the provided state.
 * (e.g. ON_GROUND, AIRBORNE, etc.)
 *
 * @author Iwo Plaza
 */
public class StateCondition<D extends IEntityData> implements ITriggerCondition<D>
{
    private final State state;

    public StateCondition(IKumoInstancingContext<D> context, Template template)
    {
        if (template.state == null)
        {
            throw new AnimationRuntimeException("No 'state' property given for trigger condition.");
        }

        this.state = template.state;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<D> context)
    {
        IEntityData entityData = context.getEntityData();

        switch (this.state)
        {
            case ON_GROUND:
                return entityData.isOnGround();
            case AIRBORNE:
                return !entityData.isOnGround();
            case SPRINTING:
                return entityData.isSprinting();
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
        public final State state;

        public Template(String type, State state)
        {
            super(type);
            this.state = state;
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new StateCondition<>(context, this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeByte((byte) state.ordinal());
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
        {
            return new Template(type, State.values()[in.readByte()]);
        }
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
