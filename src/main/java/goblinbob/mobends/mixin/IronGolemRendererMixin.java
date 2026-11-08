package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps vanilla's walking rock (a turn about Z on a triangle wave of the limb swing) off a bent iron
 * golem whose definition asks for it ({@code renderer.undoVanillaRock}), so the animator has the whole
 * model to itself. 1.12 turned the model back by as much just before vanilla turned it; here the
 * turn is left out.
 */
@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin {
    @WrapOperation(
        method = "setupRotations(Lnet/minecraft/world/entity/animal/IronGolem;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V")
    )
    private void mobends$leaveRockOut(PoseStack poseStack, Quaternionf rock, Operation<Void> original) {
        DefinedEntityData<?> bent = ((BendingEntityRenderer) this).mobends$bentData();
        if (bent != null && bent.getDefinition().renderer.undoVanillaRock) {
            return;
        }
        original.call(poseStack, rock);
    }
}
