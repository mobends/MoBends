package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrail;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^*
 * Draws an arrow's trail ({@link ArrowTrail}) before the arrow, as 1.12's {@code RenderBendsArrow} did.
 * On 26.x the trail moves on as the arrow's render state is filled, which carries its quads
 * ({@code ArrowRenderStateMixin}) to where the arrow is submitted.
 ^/
@Mixin(ArrowRenderer.class)
public abstract class ArrowRendererMixin {
    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;Lnet/minecraft/client/renderer/entity/state/ArrowRenderState;F)V",
        at = @At("TAIL")
    )
    private void mobends$advanceTrail(AbstractArrow arrow, ArrowRenderState state, float partialTicks, CallbackInfo ci) {
        ((ArrowTrail.State) state).mobends$setTrail(ArrowTrail.advance(arrow, partialTicks));
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/ArrowRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void mobends$submitTrail(ArrowRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
                                     CallbackInfo ci) {
        ArrowTrail.submit(((ArrowTrail.State) state).mobends$trail(), poseStack, collector);
    }
}
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrail;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws an arrow's trail ({@link ArrowTrail}) before the arrow, as 1.12's {@code RenderBendsArrow} did. */
@Mixin(ArrowRenderer.class)
public abstract class ArrowRendererMixin {
    @Inject(
        method = "render(Lnet/minecraft/world/entity/projectile/AbstractArrow;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void mobends$drawTrail(AbstractArrow arrow, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                                   CallbackInfo ci) {
        ArrowTrail.draw(arrow, partialTicks, poseStack, buffers);
    }
}
//?}
