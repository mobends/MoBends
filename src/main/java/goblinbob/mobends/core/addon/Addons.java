package goblinbob.mobends.core.addon;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.kumo.api.KumoRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The registered addons. Addon authors don't use it: see {@link IAddon}.
 *
 * @author Iwo Plaza
 */
public final class Addons {
    /** The Fabric entrypoint addons are listed under in their {@code fabric.mod.json}. */
    public static final String ENTRYPOINT = "mobends";

    private static final List<IAddon> ADDONS = new ArrayList<>();
    /** Addons registered before the client core existed, with their mod ids: their content is registered once it does. */
    private static final Map<IAddon, String> PENDING = new LinkedHashMap<>();
    private static boolean clientCoreCreated = false;

    private Addons() {
    }

    /**
     * Registers an addon (once per class). Its content is registered at once if the client core
     * exists, or else as soon as it does (on a server, never).
     */
    public static void registerAddon(String modId, IAddon addon) {
        for (IAddon registered : ADDONS) {
            if (registered.getClass() == addon.getClass()) {
                return;
            }
        }
        ADDONS.add(addon);
        if (clientCoreCreated) {
            registerContent(modId, addon);
        } else {
            PENDING.put(addon, modId);
        }
    }

    /** Registers the content of the addons that came before the client core; called once it exists. */
    public static void onClientCoreCreated() {
        clientCoreCreated = true;
        Map<IAddon, String> pending = new LinkedHashMap<>(PENDING);
        PENDING.clear();
        pending.forEach((addon, modId) -> registerContent(modId, addon));
    }

    private static void registerContent(String modId, IAddon addon) {
        MoBends.LOGGER.info("Mo' Bends addon {} ({})", addon.getDisplayName(), modId);
        addon.registerContent(new AddonAnimationRegistry(modId));
    }

    /** Throws if animators have started loading, after which new content would apply to some of them only. */
    static void checkRegistrationOpen() {
        KumoRegistry.checkOpen();
    }

    public static Iterable<IAddon> getRegistered() {
        return ADDONS;
    }

    public static void onRenderTick(float partialTicks) {
        forEach(addon -> addon.onRenderTick(partialTicks));
    }

    public static void onClientTick() {
        forEach(IAddon::onClientTick);
    }

    public static void onRefresh() {
        forEach(IAddon::onRefresh);
    }

    private static void forEach(Consumer<IAddon> callback) {
        for (IAddon addon : ADDONS) {
            callback.accept(addon);
        }
    }
}
