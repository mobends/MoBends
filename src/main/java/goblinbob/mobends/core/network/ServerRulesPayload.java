package goblinbob.mobends.core.network;

import goblinbob.mobends.MoBends;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * The server's rules ({@link ServerRules}, as JSON), sent to a player with Mo' Bends as they join
 * (1.12's {@code MessageConfigResponse}, which the client asked for; here the server sends them
 * unasked). Players without Mo' Bends aren't sent it.
 */
public record ServerRulesPayload(String json) implements CustomPacketPayload {
    public static final Type<ServerRulesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MoBends.MOD_ID, "server_rules"));
    public static final StreamCodec<ByteBuf, ServerRulesPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ServerRulesPayload::new, ServerRulesPayload::json);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
