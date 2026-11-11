package goblinbob.mobends.mixin;

import goblinbob.mobends.core.vanilla.PlayerModelParts;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Hands the bent layers what the player model keeps private ({@link PlayerModelParts}). */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin implements PlayerModelParts {
    @Shadow
    @Final
    private boolean slim;

    //? if <26.1 {
    @Shadow
    @Final
    private ModelPart cloak;
    //?}

    @Override
    public boolean mobends$isSlim() {
        return slim;
    }

    //? if <26.1 {
    @Override
    public ModelPart mobends$cloak() {
        return cloak;
    }
    //?}
}
