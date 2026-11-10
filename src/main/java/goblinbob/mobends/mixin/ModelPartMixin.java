package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsRenderer;
import goblinbob.mobends.core.client.model.TreeRoot;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Keeps the root of the tree the part was built in ({@link TreeRoot}), and skips the part's draw
 * while a model that draws its parts its own way is drawn bent ({@link BendsRenderer#skipsPartDraw}):
 * the bent parts are drawn in its place once it returns.
 */
@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements TreeRoot {
    @Unique
    @Nullable
    private ModelPart mobends$treeRoot;

    @Override
    @Nullable
    public ModelPart mobends$getTreeRoot() {
        return mobends$treeRoot;
    }

    @Override
    public void mobends$setTreeRoot(ModelPart root) {
        mobends$treeRoot = root;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void mobends$skipWhenBent(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        if (BendsRenderer.skipsPartDraw((ModelPart) (Object) this)) {
            ci.cancel();
        }
    }
}
