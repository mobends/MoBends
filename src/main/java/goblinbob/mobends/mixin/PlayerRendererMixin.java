package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps vanilla's swimming tilt off a bent player in water. Vanilla turns a swimming player's whole
 * model face down in {@code setupRotations}; the player animator turns the root bone for its own
 * swimming poses (1.12 had no swimming pose), and the bent model is drawn inside vanilla's turn, so
 * both would apply and a player sprint-swimming underwater would end up nearly upside down.
 * Out of water (crawling through a one-block gap) vanilla's tilt stays: the animator has no
 * crawling pose, and the tilt keeps the player inside the gap.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @WrapOperation(
        method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSwimAmount(F)F")
    )
    private float mobends$noSwimTiltWhenBent(AbstractClientPlayer player, float partialTicks, Operation<Float> original) {
        if (DefinedRenderer.replacesSwimmingTilt(player) && ((BendingEntityRenderer) this).mobends$bentData() != null) {
            return 0.0F;
        }
        return original.call(player, partialTicks);
    }
}
