package goblinbob.mobends.fabric;

//? if fabric {
import goblinbob.mobends.core.network.ServerRulesPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.function.Consumer;

/** The client's side of Mo' Bends' payloads; only loaded on a client. */
final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    /** {@code handler} gets the server's rules, on the client's thread. */
    static void receiveServerRules(Consumer<ServerRulesPayload> handler) {
        ClientPlayNetworking.registerGlobalReceiver(ServerRulesPayload.TYPE, (payload, context) -> handler.accept(payload));
    }
}
//?}
