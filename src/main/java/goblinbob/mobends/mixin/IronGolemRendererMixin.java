package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Keeps vanilla's walking rock (a turn about Z on a triangle wave of the limb swing) off a bent iron
 * golem whose definition asks for it ({@code renderer.undoVanillaRock}), so the animator has the whole
 * model to itself. 1.12 turned the model back by as much just before vanilla turned it; here the
 * turn is left out. Where its animation gives it vanilla's frame, the root bone carries the rock
 * ({@link goblinbob.mobends.core.client.definition.DefinedRenderer#vanillaFrameAsRoot}).
 ^/
@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin {
    @WrapOperation(
        method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/IronGolemRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V")
    )
    private void mobends$leaveRockOut(PoseStack poseStack, Axis axis, float degrees, Operation<Void> original,
                                      @Local(argsOnly = true) IronGolemRenderState state) {
        DefinedEntityData<?> bent = DefinedRenderer.drawnBentBy(((BentRenderState) state).mobends$bent());
        if (bent != null && bent.getDefinition().renderer.undoVanillaRock) {
            return;
        }
        original.call(poseStack, axis, degrees);
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendingEntityRenderer;
import goblinbob.mobends.core.client.definition.DefinedRenderer;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps vanilla's walking rock (a turn about Z on a triangle wave of the limb swing) off a bent iron
 * golem whose definition asks for it ({@code renderer.undoVanillaRock}), so the animator has the whole
 * model to itself. 1.12 turned the model back by as much just before vanilla turned it; here the
 * turn is left out. Where its animation gives it vanilla's frame, the root bone carries the rock
 * ({@link goblinbob.mobends.core.client.definition.DefinedRenderer#vanillaFrameAsRoot}).
 */
@Mixin(IronGolemRenderer.class)
public abstract class IronGolemRendererMixin {
    @WrapOperation(
        //? if >=1.21 {
        method = "setupRotations(Lnet/minecraft/world/entity/animal/IronGolem;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
        //?} else {
        /*method = "setupRotations(Lnet/minecraft/world/entity/animal/IronGolem;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V",
        *///?}
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V")
    )
    private void mobends$leaveRockOut(PoseStack poseStack, Quaternionf rock, Operation<Void> original) {
        DefinedEntityData<?> bent = DefinedRenderer.drawnBentBy(((BendingEntityRenderer) this).mobends$bentData());
        if (bent != null && bent.getDefinition().renderer.undoVanillaRock) {
            return;
        }
        original.call(poseStack, rock);
    }
}
//?}
