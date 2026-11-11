package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Puts the poppy a bent iron golem offers in its bent right hand ({@link BendsLayers#translateToPartOf}),
 * as 1.12's did (it drew on the model's {@code ironGolemRightArm}, which Mo' Bends had bent).
 ^/
@Mixin(IronGolemFlowerLayer.class)
public abstract class IronGolemFlowerLayerMixin {
    @WrapOperation(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/IronGolemRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentArm(ModelPart arm, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) IronGolemRenderState state) {
        DefinedEntityData<?> bent = ((BentRenderState) state).mobends$bent();
        if (bent == null || !BendsLayers.translateToPartOf(bent.getEntity(), "ironGolemRightArm", poseStack)) {
            original.call(arm, poseStack);
        }
    }
}
*///?} else {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Puts the poppy a bent iron golem offers in its bent right hand ({@link BendsLayers#translateToPartOf}),
 * as 1.12's did (it drew on the model's {@code ironGolemRightArm}, which Mo' Bends had bent).
 */
@Mixin(IronGolemFlowerLayer.class)
public abstract class IronGolemFlowerLayerMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/IronGolem;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$toBentArm(ModelPart arm, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) IronGolem golem) {
        if (!BendsLayers.translateToPartOf(golem, "ironGolemRightArm", poseStack)) {
            original.call(arm, poseStack);
        }
    }
}
//?}
