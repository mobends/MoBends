package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/^*
 * A renderer's own turns of the entity (26.x), which {@code EntityRendererMixin} works out ahead of
 * the entity's submit for {@code core:vanilla_pose}: the frame vanilla draws the model in.
 ^/
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {
    @Invoker("setupRotations")
    void mobends$setupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale);

    @Invoker("scale")
    void mobends$scale(LivingEntityRenderState state, PoseStack poseStack);
}
*///?}
