package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.kumo.api.KumoRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    /** Addons registered before the client core existed, with their mod ids: their content is registered once it does. */
    private final Map<IAddon, String> pending = new LinkedHashMap<>();

    /**
     * Registers an addon. Its content is registered at once if the client core exists, or else as
     * soon as it does (on a server, never).
     */
    public static void registerAddon(String modId, IAddon addon)
    {
        if (INSTANCE.addons.contains(addon))
            return;

        INSTANCE.addons.add(addon);

        if (CoreClient.getInstance() != null)
        {
            addon.registerContent(new AddonAnimationRegistry(modId));
        }
        else
        {
            INSTANCE.pending.put(addon, modId);
        }
    }

    /** Registers the content of the addons that came before the client core; called once it exists. */
    public static void onClientCoreCreated()
    {
        Map<IAddon, String> pending = new LinkedHashMap<>(INSTANCE.pending);
        INSTANCE.pending.clear();
        pending.forEach((addon, modId) -> addon.registerContent(new AddonAnimationRegistry(modId)));
    }

    /** Throws if animators have started loading, after which new operations and drivers would apply to some of them only. */
    static void checkRegistrationOpen()
    {
        KumoRegistry.checkOpen();
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
