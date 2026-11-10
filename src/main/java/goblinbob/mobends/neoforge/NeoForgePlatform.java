package goblinbob.mobends.neoforge;

//? if neoforge {
/*import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.addon.MoBendsAddon;
import goblinbob.mobends.core.network.ServerRulesPayload;
import goblinbob.mobends.platform.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class NeoForgePlatform implements Platform {
    /^* What gets the server's rules on a client ({@link #registerServerRules}); null on a server. ^/
    @Nullable
    private static Consumer<ServerRulesPayload> serverRulesHandler;

    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        // The loading mod list, as ModList is not there yet while mixins are being applied.
        //? if >=1.21.9 {
        /^return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        ^///?} else
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    @Nullable
    public String modName(String modId) {
        return ModList.get().getModContainerById(modId).map(mod -> mod.getModInfo().getDisplayName()).orElse(null);
    }

    //? if >=26.1 {
    /^@Override
    public boolean registersReloadListener() {
        // NeoForgeClient.registerReloadListener, as NeoForge 26.x asks.
        return true;
    }
    ^///?}

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    @Nullable
    public Path modFile() {
        var info = ModList.get().getModFileById(MoBends.MOD_ID);
        return info == null ? null : info.getFile().getFilePath();
    }

    @Override
    public void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived) {
        // Registered with the payload handlers below, which the mod's event bus asks for.
        serverRulesHandler = onClientReceived;
    }

    /^*
     * Registers Mo' Bends' payloads (the entrypoint listens on the mod's event bus): optional, so a
     * player without Mo' Bends can join a server with it, and the other way round.
     ^/
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC, (payload, context) -> {
            Consumer<ServerRulesPayload> handler = serverRulesHandler;
            if (handler != null) {
                handler.accept(payload);
            }
        });
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ServerRulesPayload payload) {
        if (player.connection.hasChannel(ServerRulesPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @Override
    public void findAddons(BiConsumer<String, IAddon> found) {
        for (ModFileScanData scan : ModList.get().getAllScanData()) {
            // The id of the mod in that jar (the first one, if it has several).
            List<IModFileInfo> files = scan.getIModInfoData();
            if (files.isEmpty() || files.get(0).getMods().isEmpty()) {
                continue;
            }
            String modId = files.get(0).getMods().get(0).getModId();
            scan.getAnnotatedBy(MoBendsAddon.class, ElementType.TYPE).forEach(annotation -> {
                String className = annotation.clazz().getClassName();
                try {
                    Class<?> addonClass = Class.forName(className, true, NeoForgePlatform.class.getClassLoader());
                    found.accept(modId, (IAddon) addonClass.getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                    MoBends.LOGGER.error("Couldn't load the Mo' Bends addon {} of {}", className, modId, e);
                }
            });
        }
    }
}
*///?}
