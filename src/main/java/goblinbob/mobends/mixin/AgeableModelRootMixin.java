package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import goblinbob.mobends.core.vanilla.ModelRootHolder;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the root of the parts of the animals' models the definitions bend (the wolf's; the
 * quadrupeds': cow, pig, sheep and its wool, polar bear; the chicken's), which vanilla drops once
 * it has its parts, as {@link HumanoidModelMixin} does for the humanoids'.
 */
@Mixin({ WolfModel.class, QuadrupedModel.class, ChickenModel.class })
public abstract class AgeableModelRootMixin implements ModelRootHolder {
    @Unique
    private ModelPart mobends$root;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mobends$keepRoot(CallbackInfo ci, @Local(argsOnly = true) ModelPart root) {
        mobends$root = root;
    }

    @Override
    public ModelPart mobends$getRoot() {
        return mobends$root;
    }
}
