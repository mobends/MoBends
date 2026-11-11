package goblinbob.mobends.mixin;

// 26.x has no list models: ModelFeatureRendererMixin draws every model.
//? if <26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.vanilla.AgeableModelLayout;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a model's bent parts in place of its own, when its renderer marked this draw as bent
 * ({@link BendsRenderer#markBentDraw}). This is the innermost point of the model's draw: whatever
 * other mods do around the renderer's call has already happened, and their consumer, light,
 * overlay and colour are the ones the bent parts draw with. {@code AgeableListModel} is where the
 * models the definitions bend ({@code HumanoidModel}, so {@code PlayerModel} and the zombies' and
 * skeletons' models) get their draw, and so do the models a bent entity's layers draw over it
 * (its armour, a drowned's outer layer, the entity's own model drawn again by a layer), which are
 * bent with it ({@link BendsLayers#drawInLayer}). It also hands over how the model draws a baby
 * ({@link AgeableModelLayout}), so a bent baby is drawn alike.
 */
@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin implements AgeableModelLayout {
    @Shadow
    @Final
    private boolean scaleHead;
    @Shadow
    @Final
    private float babyYHeadOffset;
    @Shadow
    @Final
    private float babyZHeadOffset;
    @Shadow
    @Final
    private float babyHeadScale;
    @Shadow
    @Final
    private float babyBodyScale;
    @Shadow
    @Final
    private float bodyYOffset;

    @Shadow
    protected abstract Iterable<ModelPart> headParts();

    //? if >=1.21 {
    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At("HEAD"), cancellable = true)
    private void mobends$drawBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha, CallbackInfo ci) {
        int color = VanillaDraw.argb(red, green, blue, alpha);
    *///?}
        EntityModel<?> model = (EntityModel<?>) (Object) this;
        if (BendsRenderer.drawMarked(model, poseStack, consumer, packedLight, packedOverlay, color)
                || BendsLayers.drawInLayer(model, poseStack, consumer, packedLight, packedOverlay, color)) {
            ci.cancel();
        }
    }

    @Override
    public boolean mobends$scalesHead() {
        return scaleHead;
    }

    @Override
    public float mobends$babyYHeadOffset() {
        return babyYHeadOffset;
    }

    @Override
    public float mobends$babyZHeadOffset() {
        return babyZHeadOffset;
    }

    @Override
    public float mobends$babyHeadScale() {
        return babyHeadScale;
    }

    @Override
    public float mobends$babyBodyScale() {
        return babyBodyScale;
    }

    @Override
    public float mobends$bodyYOffset() {
        return bodyYOffset;
    }

    @Override
    public Iterable<ModelPart> mobends$headParts() {
        return headParts();
    }
}
//?}
