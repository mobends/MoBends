package goblinbob.mobends.core.network;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.util.Ids;
//? if >=1.21 {
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
*///?}

/**
 * The server's rules ({@link ServerRules}, as JSON), sent to a player with Mo' Bends as they join
 * (1.12's {@code MessageConfigResponse}, which the client asked for; here the server sends them
 * unasked). Players without Mo' Bends aren't sent it.
 */
//? if >=1.21 {
public record ServerRulesPayload(String json) implements CustomPacketPayload {
    public static final Type<ServerRulesPayload> TYPE = new Type<>(Ids.of(MoBends.MOD_ID, "server_rules"));
    public static final StreamCodec<ByteBuf, ServerRulesPayload> CODEC = ByteBufCodecs.STRING_UTF8.map(ServerRulesPayload::new, ServerRulesPayload::json);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
//?} else {
/*public record ServerRulesPayload(String json) {
    /^* The channel it's sent on. *^/
    public static final ResourceLocation ID = Ids.of(MoBends.MOD_ID, "server_rules");

    /^* Writes it as 1.21's codec does: one UTF-8 string. *^/
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(json);
    }

    public static ServerRulesPayload read(FriendlyByteBuf buffer) {
        return new ServerRulesPayload(buffer.readUtf());
    }
}
*///?}
