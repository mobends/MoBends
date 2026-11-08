package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Draws an armour model that isn't made of its vanilla parts' boxes (GeckoLib's draws its own mesh,
 * leaving the parts empty), so swapping the parts can't pose it. It draws itself into the parts of
 * the wrapper instead, each frame after the wrapper has synced them up with the animation
 * ({@link IPartWrapper#applyTransform}).
 */
public interface IArmorGeometry
{
    void render(ArmorWrapper wrapper, EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, float scale);
}
