package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Draws a bent entity's models bent, as they're drawn from their submits (26.x draws an entity's
 * models after its renderer ran): the entity's own model as its definition bends it, and a model a
 * layer submitted (armour, a copy of the model) in the frame the layer was in.
 ^/
@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin {
    @WrapOperation(
        method = "prepareModel(Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Submit;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
        )
    )
    private void mobends$drawBent(Model<?> model, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color,
                                  Operation<Void> original, @Local(argsOnly = true) ModelFeatureRenderer.Submit<?> submit) {
        if (submit.state() instanceof BentRenderState state && model instanceof EntityModel<?> entityModel) {
            DefinedEntityData<?> bent = state.mobends$bent();
            if (bent != null && drawBent(state, submit, bent, entityModel, poseStack, consumer, packedLight, packedOverlay, color)) {
                return;
            }
        }
        original.call(model, poseStack, consumer, packedLight, packedOverlay, color);
    }

    /^* Whether it drew {@code model} bent, and vanilla's draw must be skipped. ^/
    private static boolean drawBent(BentRenderState state, ModelFeatureRenderer.Submit<?> submit, DefinedEntityData<?> bent, EntityModel<?> model,
                                    PoseStack poseStack,
                                    VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        if (model == state.mobends$model()) {
            BendsRenderer.markBentDraw(bent, model, state.mobends$entityFrame(), state.mobends$bodyYaw());
            try {
                return BendsRenderer.drawMarked(model, poseStack, consumer, packedLight, packedOverlay, color);
            } finally {
                BendsRenderer.clearBentDraw();
            }
        }
        BendsLayers.Frame frame = state.mobends$layerFrames().get(submit);
        return frame != null && BendsLayers.drawInLayer(frame, model, poseStack, consumer, packedLight, packedOverlay, color);
    }
}
*///?}
