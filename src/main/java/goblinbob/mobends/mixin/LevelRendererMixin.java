package goblinbob.mobends.mixin;

// On 26.x the debug rays are vanilla's gizmos (LevelExtractorMixin).
//? if <26.1 {
import goblinbob.mobends.core.client.event.RayGizmoRenderer;
import net.minecraft.client.Camera;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the debug rays of the animators ({@link RayGizmoRenderer}) once the world is drawn. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    //? if >=1.21 {
    @Inject(
        method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
        at = @At("TAIL")
    )
    private void mobends$drawRays(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
                                  Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        RayGizmoRenderer.render(camera, frustumMatrix);
    }
    //?} else {
    /*@Inject(
        method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
        at = @At("TAIL")
    )
    private void mobends$drawRays(PoseStack poseStack, float partialTick, long nanoTime, boolean renderBlockOutline, Camera camera,
                                  GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        // The camera's turn is on the pose stack the world was drawn with.
        RayGizmoRenderer.render(camera, poseStack.last().pose());
    }
    *///?}
}
//?}
