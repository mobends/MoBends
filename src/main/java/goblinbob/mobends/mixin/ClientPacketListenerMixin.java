package goblinbob.mobends.mixin;

import goblinbob.mobends.core.client.AnimationPolicy;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A world was joined ({@link AnimationPolicy#onWorldJoin}): the server's rules go back to the
 * defaults until its own arrive, which it sends after this. Vanilla handles the login on the
 * network thread first, then again on the client's, which is the one that gets this far.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V", at = @At("TAIL"))
    private void mobends$joined(ClientboundLoginPacket packet, CallbackInfo ci) {
        AnimationPolicy.INSTANCE.onWorldJoin();
    }
}
