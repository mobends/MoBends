package goblinbob.mobends.platform;

import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.network.ServerRulesPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * What the loader-independent code needs from the mod loader. Each loader implements it in its own
 * package; the one built into the jar is picked below.
 */
public interface Platform {
    Platform INSTANCE =
        //? if fabric {
        new goblinbob.mobends.fabric.FabricPlatform();
        //?} else if neoforge {
        /*new goblinbob.mobends.neoforge.NeoForgePlatform();
        *///?} else {
        /*new goblinbob.mobends.forge.ForgePlatform();
        *///?}

    /** The loader's name, as in the jar names: {@code fabric}, {@code neoforge} or {@code forge}. */
    String loaderName();

    boolean isModLoaded(String modId);

    /** The name a mod gives itself ({@code minecraft}'s is Minecraft), or null if no mod has this id. */
    @Nullable
    String modName(String modId);

    /**
     * Whether the loader gives Mo' Bends' resource reload listener (which reads the definitions and
     * animators again) its place itself: NeoForge 26.x, which wants every listener named. Otherwise
     * {@code MinecraftMixin} adds it with the game's own.
     */
    default boolean registersReloadListener() {
        return false;
    }

    /** Whether the game is a client (the server has no rendering, so animation stays off there). */
    boolean isClient();

    /** The directory the game keeps mod configuration in. */
    Path configDir();

    /**
     * Where Mo' Bends was loaded from: its jar, a directory in development, or null if the loader
     * can't say (the jar is inside another). Its checksum goes to the mobends.com API.
     */
    @Nullable
    Path modFile();

    /**
     * Registers the payload a server sends its rules in ({@link ServerRulesPayload}); on a client,
     * {@code onClientReceived} gets each one, on the client's thread (null on a server). Once, at
     * start-up.
     */
    void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived);

    /** Sends {@code payload} to {@code player} if their game has Mo' Bends (knows the payload); else nothing. */
    void sendToPlayer(ServerPlayer player, ServerRulesPayload payload);

    /**
     * Gives {@code found} every other mod's addon, with that mod's id (docs/addons.md): on Fabric,
     * the {@code "mobends"} entrypoints; on NeoForge and Forge, the classes marked
     * {@link goblinbob.mobends.core.addon.MoBendsAddon}. An addon that can't be made is logged and
     * skipped. Once, on the client, at start-up.
     */
    void findAddons(BiConsumer<String, IAddon> found);
}
