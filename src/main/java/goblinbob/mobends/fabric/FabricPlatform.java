package goblinbob.mobends.fabric;

//? if fabric {
import goblinbob.mobends.core.network.ServerRulesPayload;
import goblinbob.mobends.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class FabricPlatform implements Platform {
    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived) {
        PayloadTypeRegistry.playS2C().register(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC);
        if (onClientReceived != null) {
            // In a class of its own, so a server never loads the client's networking.
            FabricClientNetworking.receiveServerRules(onClientReceived);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ServerRulesPayload payload) {
        if (ServerPlayNetworking.canSend(player, ServerRulesPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
//?}
