package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Met when the item in a player's equipment slot has a display name matching the pattern.
 * This is the one Minecraft-specific condition in the core; it only applies to entity data.
 *
 * @author Iwo Plaza
 */
public class EquipmentNameCondition implements ITriggerCondition
{

    private String namePattern;
    private EntityEquipmentSlot slot;

    public EquipmentNameCondition(Template template)
    {
        this.namePattern = template.namePattern;
        this.slot = template.slot;
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        if (!(context.getSubject() instanceof EntityData))
        {
            return false;
        }

        Entity entity = ((EntityData<?>) context.getSubject()).getEntity();

        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack itemStack = player.getItemStackFromSlot(this.slot);
            return itemStack.getDisplayName().matches(namePattern);
        }

        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public String namePattern;
        public EntityEquipmentSlot slot;

    }

}
