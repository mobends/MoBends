package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.entity.EntityLivingBase;

/** LAB SHIM. No web API; every entity gets the default trail colour. */
public class SupporterContent
{
    public static IColorRead getTrailColorFor(EntityLivingBase entity)
    {
        return Color.WHITE;
    }
}
