package goblinbob.mobends.mixin;

import goblinbob.mobends.core.vanilla.ArmorLayerModels;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Tells which armour models are the layer's own vanilla ones ({@link ArmorLayerModels}). */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin implements ArmorLayerModels {
    @Shadow
    @Final
    private HumanoidModel<?> innerModel;

    @Shadow
    @Final
    private HumanoidModel<?> outerModel;

    @Override
    public HumanoidModel<?> mobends$innerModel() {
        return innerModel;
    }

    @Override
    public HumanoidModel<?> mobends$outerModel() {
        return outerModel;
    }
}
