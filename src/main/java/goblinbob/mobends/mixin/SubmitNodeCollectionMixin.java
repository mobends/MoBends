package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/^*
 * Keeps, with a bent entity's render state, the frame of the layer that submits a model now, by the
 * submit, so the model is drawn in it later in the frame ({@code ModelFeatureRendererMixin}): a layer
 * runs as the entity is submitted, its models are drawn after.
 ^/
@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin {
    @ModifyExpressionValue(
        method = "submitModel",
        at = @At(value = "NEW", target = "net/minecraft/client/renderer/feature/ModelFeatureRenderer$Submit")
    )
    private ModelFeatureRenderer.Submit<?> mobends$keepLayerFrame(ModelFeatureRenderer.Submit<?> submit) {
        BendsLayers.Frame frame = BendsLayers.frameForSubmit();
        if (frame != null && submit.state() instanceof BentRenderState bent && bent.mobends$bent() != null) {
            bent.mobends$layerFrames().put(submit, frame);
        }
        return submit;
    }
}
*///?}
