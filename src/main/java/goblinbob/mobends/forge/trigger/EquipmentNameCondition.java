package goblinbob.mobends.forge.trigger;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.trigger.ITriggerCondition;
import goblinbob.mobends.core.kumo.trigger.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.trigger.TriggerConditionTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.forge.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.io.IOException;

/**
 *
 * @author Iwo Plaza
 */
public class EquipmentNameCondition implements ITriggerCondition<EntityData>
{
    private String namePattern;
    private EquipmentSlotType slot;

    public EquipmentNameCondition(Template template)
    {
        this.namePattern = template.namePattern;
        this.slot = template.slot;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext<EntityData> context)
    {
        Entity entity = context.getEntityData().getEntity();

        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity;
            ItemStack itemStack = player.getItemStackFromSlot(this.slot);
            return itemStack.getDisplayName().getString().matches(namePattern);
        }

        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public String namePattern;
        public EquipmentSlotType slot;

        public Template(String type)
        {
            super(type);
        }

        @Override
        public <D extends IEntityData> ITriggerCondition<D> instantiate(IKumoInstancingContext<D> context)
        {
            //noinspection unchecked
            return (ITriggerCondition<D>) new EquipmentNameCondition(this);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeString(namePattern);
            out.writeByte((byte) slot.ordinal());
        }

        public static Template deserialize(ISerialContext<EntityData> context, String type, ISerialInput in) throws IOException
        {
            Template template = new Template(type);

            template.namePattern = in.readString();
            template.slot = EquipmentSlotType.values()[in.readByte()];

            return template;
        }
    }
}
