package goblinbob.mobends.mixin;

// Not ported to 26.x yet (the arrow trails).
//? if <26.1 {
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
