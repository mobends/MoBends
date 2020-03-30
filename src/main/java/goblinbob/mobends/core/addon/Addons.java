package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.bender.AddonAnimationRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The class responsible for managing the registered addons.
 *
 * -- FOR ADDON DEVELOPERS: -- Don't use this class directly. Use the AddonHelper class instead.
 *
 * @author Iwo Plaza
 */
public class Addons
{

    private static final Addons INSTANCE = new Addons();

    private final List<IAddon> addons = new ArrayList<>();

    public static void registerAddon(String modId, IAddon addon)
    {
        if (INSTANCE.addons.contains(addon))
            return;

        INSTANCE.addons.add(addon);

        if (CoreClient.getInstance() != null)
        {
            addon.registerAnimatedEntities(new AddonAnimationRegistry(modId));
        }
    }

    public static Iterable<IAddon> getRegistered()
    {
        return INSTANCE.addons;
    }

    public static void onRenderTick(float partialTicks)
    {
        INSTANCE.addons.forEach(addon -> addon.onRenderTick(partialTicks));
    }

    public static void onClientTick()
    {
        INSTANCE.addons.forEach(IAddon::onClientTick);
    }

    public static void onRefresh()
    {
        INSTANCE.addons.forEach(IAddon::onRefresh);
    }

}
