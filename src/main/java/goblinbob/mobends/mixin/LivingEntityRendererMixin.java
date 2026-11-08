package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/** Poses the entities Mo' Bends animates as their render starts. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    /**
     * The data of the entity this renderer is drawing, posed for this frame; null while it draws
     * one as vanilla does. A renderer draws one entity at a time, on the render thread.
     */
    @Unique
    @Nullable
    private DefinedEntityData<?> mobends$bent;

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void mobends$pose(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight, CallbackInfo ci) {
        mobends$bent = BendsRenderer.beforeRender(entity, partialTicks);
    }
}
