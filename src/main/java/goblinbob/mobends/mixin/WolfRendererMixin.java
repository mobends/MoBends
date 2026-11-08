package goblinbob.mobends.mixin;

import goblinbob.mobends.standard.client.renderer.entity.layers.WolfMiscLayer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives the wolf's renderer Mo' Bends' layer of the wolf's mouth ({@link WolfMiscLayer}), which 1.12
 * added for the definitions that ask for it. It draws nothing for a wolf drawn as vanilla. (It
 * extends the renderer's superclass to reach {@code addLayer}, which isn't public on every loader.)
 */
@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin extends MobRenderer<Wolf, WolfModel<Wolf>> {
    private WolfRendererMixin(EntityRendererProvider.Context context, WolfModel<Wolf> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mobends$addMiscLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        addLayer(new WolfMiscLayer(this));
    }
}
