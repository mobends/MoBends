package goblinbob.mobends.mixin;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.network.ServerRules;
import goblinbob.mobends.core.network.ServerRulesPayload;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends the server's rules ({@link ServerRules}) to each player who joins with Mo' Bends, once they're in the world. */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
        method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V",
        at = @At("TAIL")
    )
    private void mobends$sendRules(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        MoBends.platform().sendToPlayer(player, new ServerRulesPayload(ServerRules.server().toJson()));
    }
}
