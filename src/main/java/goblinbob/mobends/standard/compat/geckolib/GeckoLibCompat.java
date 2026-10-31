package goblinbob.mobends.standard.compat.geckolib;

import goblinbob.mobends.standard.client.model.armor.IArmorGeometry;
import net.minecraft.client.model.ModelBiped;

import javax.annotation.Nullable;

/**
 * Mo' Bends with GeckoLib (3, for 1.12.2), which it doesn't need: this class never touches
 * GeckoLib's, so it can be called whether GeckoLib is installed or not, and only reaches the
 * classes of this package that do when it is.
 */
public final class GeckoLibCompat
{
    private static final String ARMOR_RENDERER = "software.bernie.geckolib3.renderers.geo.GeoArmorRenderer";

    private static Boolean present;

    private GeckoLibCompat()
    {
    }

    /** Whether GeckoLib's classes are there (an armour model can only be GeckoLib's if they are). */
    public static boolean isPresent()
    {
        if (present == null)
        {
            try
            {
                Class.forName(ARMOR_RENDERER, false, GeckoLibCompat.class.getClassLoader());
                present = true;
            }
            catch (ClassNotFoundException | LinkageError e)
            {
                present = false;
            }
        }
        return present;
    }

    /** What draws {@code model} posed, when it is GeckoLib's armour; otherwise null. */
    @Nullable
    public static IArmorGeometry geometryFor(ModelBiped model)
    {
        return isPresent() ? GeoArmorGeometry.createFor(model) : null;
    }
}
