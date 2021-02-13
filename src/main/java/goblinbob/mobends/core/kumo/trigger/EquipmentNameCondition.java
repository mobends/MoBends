package goblinbob.mobends.core.kumo.trigger;

import goblinbob.mobends.core.kumo.TriggerConditionTemplate;
import goblinbob.mobends.forge.EntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

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
    }
}
