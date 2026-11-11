package goblinbob.mobends.fabric;

//? if fabric {
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.network.ServerRulesPayload;
import goblinbob.mobends.platform.Platform;
import net.fabricmc.api.EnvType;
//? if >=1.21 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
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
    @Nullable
    public String modName(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(mod -> mod.getMetadata().getName()).orElse(null);
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
    @Nullable
    public Path modFile() {
        return FabricLoader.getInstance().getModContainer(MoBends.MOD_ID)
            .map(container -> container.getOrigin())
            .filter(origin -> origin.getKind() == ModOrigin.Kind.PATH && !origin.getPaths().isEmpty())
            .map(origin -> origin.getPaths().get(0))
            .orElse(null);
    }

    @Override
    public void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived) {
        //? if >=1.21 {
        //? if >=26.1 {
        /*PayloadTypeRegistry.clientboundPlay().register(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC);
        *///?} else {
        PayloadTypeRegistry.playS2C().register(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC);
        //?}
        //?}
        if (onClientReceived != null) {
            // In a class of its own, so a server never loads the client's networking.
            FabricClientNetworking.receiveServerRules(onClientReceived);
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ServerRulesPayload payload) {
        //? if >=1.21 {
        if (ServerPlayNetworking.canSend(player, ServerRulesPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
        //?} else {
        /*if (ServerPlayNetworking.canSend(player, ServerRulesPayload.ID)) {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            payload.write(buffer);
            ServerPlayNetworking.send(player, ServerRulesPayload.ID, buffer);
        }
        *///?}
    }

    @Override
    public void findAddons(BiConsumer<String, IAddon> found) {
        List<EntrypointContainer<IAddon>> containers;
        try {
            containers = FabricLoader.getInstance().getEntrypointContainers(Addons.ENTRYPOINT, IAddon.class);
        } catch (RuntimeException e) {
            MoBends.LOGGER.error("Couldn't load the Mo' Bends addons", e);
            return;
        }
        for (EntrypointContainer<IAddon> container : containers) {
            String modId = container.getProvider().getMetadata().getId();
            try {
                found.accept(modId, container.getEntrypoint());
            } catch (RuntimeException e) {
                MoBends.LOGGER.error("Couldn't load the Mo' Bends addon of {}", modId, e);
            }
        }
    }
}
//?}
