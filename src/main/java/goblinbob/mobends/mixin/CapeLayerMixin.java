package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Submits a bent player's cape on the bent body's back ({@link BendsLayers#placeCape}). On 26.x the
 * cape is a model of its own, drawn later in the frame; its ripple ({@code CapeWave}) and standing it
 * up as 1.12 did aren't ported yet.
 ^/
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;III)V"
        )
    )
    private void mobends$bentCape(SubmitNodeCollector collector, Model<?> model, Object state, PoseStack poseStack, RenderType renderType,
                                  int lightCoords, int overlayCoords, int outlineColor, Operation<Void> original) {
        DefinedEntityData<?> bent = state instanceof BentRenderState bentState ? bentState.mobends$bent() : null;
        poseStack.pushPose();
        try {
            if (bent != null) {
                BendsLayers.placeCape(bent.getEntity(), poseStack);
            }
            original.call(collector, model, state, poseStack, renderType, lightCoords, overlayCoords, outlineColor);
        } finally {
            poseStack.popPose();
        }
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.vanilla.PlayerModelParts;
import goblinbob.mobends.standard.client.renderer.entity.BendsCapeRenderer;
import goblinbob.mobends.standard.data.CapeWave;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hangs a bent player's cape from the bent body, turned by the {@code cape} bone
 * ({@link BendsLayers#placeCape}), in place of vanilla's swing: the {@code mobends:cape} driver
 * gives the bone vanilla's swing already, so it shows as vanilla's does where the animator leaves
 * it, and the animator can change it. With a {@code capeWave} component (the player's), the cape
 * ripples, drawn in strips ({@link BendsCapeRenderer}) in place of vanilla's box. Vanilla's layer
 * still decides whether the cape is drawn, and with which texture.
 */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/PlayerModel;renderCloak(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
        )
    )
    private void mobends$bentCape(PlayerModel<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                  Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayer player) {
        if (!(model instanceof PlayerModelParts parts) || !BendsLayers.placeCape(player, poseStack)) {
            original.call(model, poseStack, consumer, packedLight, packedOverlay);
            return;
        }
        ModelPart cloak = parts.mobends$cloak();
        float y = cloak.y;
        float z = cloak.z;
        BendsLayers.standCape(player, cloak);
        try {
            CapeWave wave = BendsLayers.componentOf(player, "capeWave", CapeWave.class);
            if (wave != null) {
                // Rippling, in strips, as 1.12 drew it.
                BendsCapeRenderer.render(cloak, wave.getPhase(), poseStack, consumer, packedLight, packedOverlay);
            } else {
                original.call(model, poseStack, consumer, packedLight, packedOverlay);
            }
        } finally {
            cloak.y = y;
            cloak.z = z;
        }
    }
}
//?}
