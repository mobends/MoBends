package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Draws the bent arm as the first-person hand ({@link BendsRenderer#drawFirstPersonArm}), and keeps
 * vanilla's swimming tilt off a bent player in water. Vanilla turns a swimming player's whole
 * model face down in {@code setupRotations}; the player animator turns the root bone for its own
 * swimming poses (1.12 had no swimming pose), and the bent model is drawn inside vanilla's turn, so
 * both would apply and a player sprint-swimming underwater would end up nearly upside down.
 * Out of water (crawling through a one-block gap) vanilla's tilt stays: the animator has no
 * crawling pose, and the tilt keeps the player inside the gap.
 */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    private static final String RENDER_HAND =
        "renderHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V";
    private static final String MODEL_PART_RENDER =
        "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V";

    /** The first-person hand's arm: the bent arm, at rest. */
    @WrapOperation(method = RENDER_HAND, at = @At(value = "INVOKE", target = MODEL_PART_RENDER, ordinal = 0))
    private void mobends$bentHand(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                  Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayer player) {
        PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
        if (!BendsRenderer.drawFirstPersonArm(player, model, part, false, poseStack, consumer, packedLight, packedOverlay)) {
            original.call(part, poseStack, consumer, packedLight, packedOverlay);
        }
    }

    /** The first-person hand's sleeve: the bent sleeve, over the bent arm. */
    @WrapOperation(method = RENDER_HAND, at = @At(value = "INVOKE", target = MODEL_PART_RENDER, ordinal = 1))
    private void mobends$bentSleeve(ModelPart part, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                    Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayer player) {
        PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
        if (!BendsRenderer.drawFirstPersonArm(player, model, part, true, poseStack, consumer, packedLight, packedOverlay)) {
            original.call(part, poseStack, consumer, packedLight, packedOverlay);
        }
    }

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
