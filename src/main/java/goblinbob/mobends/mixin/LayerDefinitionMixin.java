package goblinbob.mobends.mixin;

import goblinbob.mobends.core.client.model.ModelRoots;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Records every part tree the game builds ({@link ModelRoots}), so any model's root can be found. */
@Mixin(LayerDefinition.class)
public abstract class LayerDefinitionMixin {
    @Inject(method = "bakeRoot", at = @At("RETURN"))
    private void mobends$recordRoot(CallbackInfoReturnable<ModelPart> cir) {
        ModelRoots.record(cir.getReturnValue());
    }
}
