package goblinbob.mobends.standard.main;

import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

/** LAB SHIM. Static defaults of the mod configuration; scenarios may change them. */
public class ModConfig
{
    public static boolean showArrowTrails = true;
    public static boolean showSwordTrail = true;
    public static boolean performSpinAttack = true;

    public static UseActionType getItemUseAction(Item item)
    {
        return null;
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        return null;
    }

    public static boolean shouldKeepArmorAsVanilla(Item item)
    {
        return false;
    }

    public static boolean shouldKeepEntityAsVanilla(Entity entity)
    {
        return false;
    }
}
