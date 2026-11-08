package goblinbob.mobends;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's loader-independent entry point. Each loader's entrypoint ({@code goblinbob.mobends.fabric},
 * {@code goblinbob.mobends.neoforge}) calls {@link #init()}; everything outside those packages must not
 * use a loader API (see CONTRIBUTING.md), and reaches the loader through
 * {@link goblinbob.mobends.platform.Platform} instead.
 */
public final class MoBends {
    public static final String MOD_ID = "mobends";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/"2.0.0-alpha.1";
    public static final String MINECRAFT = /*$ minecraft*/"1.21.1";

    private MoBends() {
    }

    public static void init() {
        LOGGER.info("Mo' Bends {} for Minecraft {} on {}", VERSION, MINECRAFT, platform().loaderName());
        if (platform().isClient()) {
            // In a class of its own, so a server never loads the client's classes.
            CoreClient.init();
        }
    }

    public static Platform platform() {
        return Platform.INSTANCE;
    }
}
