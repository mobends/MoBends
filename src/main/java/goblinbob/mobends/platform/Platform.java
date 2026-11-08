package goblinbob.mobends.platform;

import java.nio.file.Path;

/**
 * What the loader-independent code needs from the mod loader. Each loader implements it in its own
 * package; the one built into the jar is picked below.
 */
public interface Platform {
    Platform INSTANCE =
        //? if fabric {
        new goblinbob.mobends.fabric.FabricPlatform();
        //?} else {
        /*new goblinbob.mobends.neoforge.NeoForgePlatform();
        *///?}

    /** The loader's name, as in the jar names: {@code fabric} or {@code neoforge}. */
    String loaderName();

    boolean isModLoaded(String modId);

    /** Whether the game is a client (the server has no rendering, so animation stays off there). */
    boolean isClient();

    /** The directory the game keeps mod configuration in. */
    Path configDir();
}
