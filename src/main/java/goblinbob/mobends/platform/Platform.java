package goblinbob.mobends.platform;

import goblinbob.mobends.core.network.ServerRulesPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
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

    /**
     * Registers the payload a server sends its rules in ({@link ServerRulesPayload}); on a client,
     * {@code onClientReceived} gets each one, on the client's thread (null on a server). Once, at
     * start-up.
     */
    void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived);

    /** Sends {@code payload} to {@code player} if their game has Mo' Bends (knows the payload); else nothing. */
    void sendToPlayer(ServerPlayer player, ServerRulesPayload payload);
}
