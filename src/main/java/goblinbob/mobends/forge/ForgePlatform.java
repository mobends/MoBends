package goblinbob.mobends.forge;

//? if forge {
/*import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.addon.MoBendsAddon;
import goblinbob.mobends.core.network.ServerRulesPayload;
import goblinbob.mobends.platform.Platform;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ForgePlatform implements Platform {
    private static final String PROTOCOL = "1";
    private static final Type ADDON_ANNOTATION = Type.getType(MoBendsAddon.class);

    /^* The server's rules' channel ({@link #registerServerRules}); null until then. ^/
    @Nullable
    private static EventNetworkChannel serverRules;

    @Override
    public String loaderName() {
        return "forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        // The loading mod list, as ModList is not there yet while mixins are being applied.
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    @Nullable
    public String modName(String modId) {
        return ModList.get().getModContainerById(modId).map(mod -> mod.getModInfo().getDisplayName()).orElse(null);
    }

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
        IModFileInfo info = ModList.get().getModFileById(MoBends.MOD_ID);
        return info == null ? null : info.getFile().getFilePath();
    }

    /^*
     * A plain channel, not a SimpleChannel, so the payload is the same bytes Fabric's 1.20.1 build
     * sends (one UTF-8 string, no message index). Optional on both sides, so a player without
     * Mo' Bends can join a server with it, and the other way round.
     ^/
    @Override
    public void registerServerRules(@Nullable Consumer<ServerRulesPayload> onClientReceived) {
        EventNetworkChannel channel = NetworkRegistry.ChannelBuilder.named(ServerRulesPayload.ID)
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .eventNetworkChannel();
        if (onClientReceived != null) {
            channel.addListener((NetworkEvent event) -> {
                // Only what a server sends; read here, on the network thread, handled on the client's.
                if (event instanceof NetworkEvent.ServerCustomPayloadEvent && event.getPayload() != null) {
                    ServerRulesPayload payload = ServerRulesPayload.read(event.getPayload());
                    NetworkEvent.Context context = event.getSource().get();
                    context.enqueueWork(() -> onClientReceived.accept(payload));
                    context.setPacketHandled(true);
                }
            });
        }
        serverRules = channel;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ServerRulesPayload payload) {
        EventNetworkChannel channel = serverRules;
        if (channel != null && channel.isRemotePresent(player.connection.connection)) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            payload.write(buffer);
            player.connection.send(new ClientboundCustomPayloadPacket(ServerRulesPayload.ID, buffer));
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
            for (ModFileScanData.AnnotationData annotation : scan.getAnnotations()) {
                if (!ADDON_ANNOTATION.equals(annotation.annotationType()) || annotation.targetType() != ElementType.TYPE) {
                    continue;
                }
                String className = annotation.clazz().getClassName();
                try {
                    Class<?> addonClass = Class.forName(className, true, ForgePlatform.class.getClassLoader());
                    found.accept(modId, (IAddon) addonClass.getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                    MoBends.LOGGER.error("Couldn't load the Mo' Bends addon {} of {}", className, modId, e);
                }
            }
        }
    }
}
*///?}
