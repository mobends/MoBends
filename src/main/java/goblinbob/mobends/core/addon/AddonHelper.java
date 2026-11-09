package goblinbob.mobends.core.addon;

/**
 * Registers an addon by hand, as 1.12's addons did. Rarely needed: addons are found on their own
 * (see {@link IAddon}). Call it on the client, before the first entity is drawn.
 *
 * @author Iwo Plaza
 */
public final class AddonHelper {
    private AddonHelper() {
    }

    public static void registerAddon(String modId, IAddon addon) {
        Addons.registerAddon(modId, addon);
    }
}
