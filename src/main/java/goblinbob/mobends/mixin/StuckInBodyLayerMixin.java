package goblinbob.mobends.mixin;

// Not ported to 26.x yet.
//? if <26.1 {
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.BendsLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sticks the arrows and bee stingers in a bent player into the bent parts
 * ({@link BendsLayers#stickIn}): vanilla picks one of the model's parts, then one of its boxes, and
 * moves into that part; for a bent player, the part and box are picked among the bent parts', and the
 * move is into the bent part, as it's posed. Vanilla still decides how many there are and where in
 * the box each goes, and draws them.
 */
@Mixin(StuckInBodyLayer.class)
public abstract class StuckInBodyLayerMixin {
    private static final String RENDER =
        "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V";

    /** Whether the last pick was of a bent part, whose frame the pose is already in. Render thread only. */
    @Unique
    private boolean mobends$inBentPart;

    @WrapOperation(
        method = RENDER,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;getRandomCube(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/client/model/geom/ModelPart$Cube;")
    )
    private ModelPart.Cube mobends$pickBentBox(ModelPart part, RandomSource random, Operation<ModelPart.Cube> original,
                                               @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) LivingEntity entity) {
        ModelPart.Cube cube = BendsLayers.stickIn(entity, random, poseStack);
        mobends$inBentPart = cube != null;
        return cube != null ? cube : original.call(part, random);
    }

    @WrapOperation(
        method = RENDER,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void mobends$intoBentPart(ModelPart part, PoseStack poseStack, Operation<Void> original) {
        if (mobends$inBentPart) {
            mobends$inBentPart = false;
            return;
        }
        original.call(part, poseStack);
    }
}
//?}
