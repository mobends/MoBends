package goblinbob.mobends.mixin;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The start of every rendered frame, before anything of it is drawn. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void mobends$beginFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // The partial tick the world's entities are drawn at (LevelRenderer.renderLevel).
        DataUpdateHandler.onRenderFrame(deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
