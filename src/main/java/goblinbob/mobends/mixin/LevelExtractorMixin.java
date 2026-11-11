package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import goblinbob.mobends.core.client.event.RayGizmoRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.gizmos.Gizmos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^*
 * Adds the animators' debug rays ({@link RayGizmoRenderer}) to the frame's gizmos, once the entities
 * are animated (as their render states were filled), just before the gizmos are handed to the
 * level's renderer.
 ^/
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Shadow
    public abstract Gizmos.TemporaryCollection collectPerFrameMainThreadGizmos();

    @Inject(method = "extractGizmos()V", at = @At("HEAD"))
    private void mobends$addRays(CallbackInfo ci) {
        try (Gizmos.TemporaryCollection ignored = collectPerFrameMainThreadGizmos()) {
            RayGizmoRenderer.emit();
        }
    }
}
*///?}
