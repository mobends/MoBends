package goblinbob.mobends.forge.addon;

import goblinbob.mobends.forge.BenderProvider;
import goblinbob.mobends.forge.SerialContext;

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

    private BenderProvider<SerialContext> benderProvider;
    private final List<IAddon> addons = new ArrayList<>();

    public static void setBenderProvider(BenderProvider<SerialContext> benderProvider)
    {
        INSTANCE.benderProvider = benderProvider;
    }

    public static void registerAddon(String modId, IAddon addon)
    {
        if (INSTANCE.addons.contains(addon))
            return;

        INSTANCE.addons.add(addon);

        AddonContentRegistry registry = new AddonContentRegistry(modId);
        addon.registerContent(registry);
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
