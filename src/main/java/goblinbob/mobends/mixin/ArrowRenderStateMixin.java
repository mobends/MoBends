package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import goblinbob.mobends.standard.client.renderer.entity.ArrowTrail;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

/^* Gives an arrow's render state room for its trail's quads ({@link ArrowTrail.State}), from where it's filled to where it's submitted. ^/
@Mixin(ArrowRenderState.class)
public abstract class ArrowRenderStateMixin implements ArrowTrail.State {
    @Unique
    @Nullable
    private float[] mobends$trail;

    @Override
    @Nullable
    public float[] mobends$trail() {
        return mobends$trail;
    }

    @Override
    public void mobends$setTrail(@Nullable float[] vertices) {
        mobends$trail = vertices;
    }
}
*///?}
