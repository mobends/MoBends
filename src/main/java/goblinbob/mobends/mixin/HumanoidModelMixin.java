package goblinbob.mobends.mixin;

import goblinbob.mobends.core.vanilla.ModelRootHolder;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/** Keeps the root of a humanoid model's parts (the player's included), which vanilla drops once it has its parts. */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin implements ModelRootHolder {
    @Unique
    private ModelPart mobends$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("TAIL"))
    private void mobends$keepRoot(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
        mobends$root = root;
    }

    @Override
    public ModelPart mobends$getRoot() {
        return mobends$root;
    }
}
