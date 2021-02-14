package goblinbob.mobends.standard.main.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;
import goblinbob.mobends.core.kumo.trigger.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.trigger.NotCondition;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.forge.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;

import java.io.IOException;

/**
 * This condition is met once the wolf is in the provided state.
 * (e.g. SITTING, etc.)
 *
 * @author Iwo Plaza
 */
public class WolfStateCondition implements ITriggerCondition<EntityData>
{
    private final WolfStateCondition.State state;

    public WolfStateCondition(WolfStateCondition.Template template)
    {
        if (template.state == null)
        {
            throw new AnimationRuntimeException("No 'state' property given for trigger condition.");
        }

        this.state = template.state;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<EntityData> context)
    {
        EntityData data = context.getEntityData();
        Entity entity = data.getEntity();

        if (!(entity instanceof WolfEntity))
        {
            throw new AnimationRuntimeException("A wolf_state trigger condition was used on something other than a wolf.");
        }

        WolfEntity wolf = (WolfEntity) entity;

        switch (this.state)
        {
            case SITTING:
                return wolf.func_233684_eK_();
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
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeByte((byte) state.ordinal());
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            //noinspection unchecked
            return (ITriggerCondition<D>) new WolfStateCondition(this);
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
        {
            return new Template(type, State.values()[in.readByte()]);
        }
    }

    public enum State
    {
        SITTING,
    }
}
