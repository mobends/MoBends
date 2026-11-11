package goblinbob.mobends.mixin;

//? if >=26.1 {
/*import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.BentRenderState;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/^* Gives every render state the room Mo' Bends needs to draw its entity bent later in the frame ({@link BentRenderState}). ^/
@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements BentRenderState {
    @Unique
    @Nullable
    private DefinedEntityData<?> mobends$bent;
    @Unique
    @Nullable
    private EntityModel<?> mobends$model;
    @Unique
    private final Matrix4f mobends$entityFrame = new Matrix4f();
    @Unique
    private float mobends$bodyYaw;
    @Unique
    private final Map<Object, BendsLayers.Frame> mobends$layerFrames = new IdentityHashMap<>();

    @Override
    @Nullable
    public DefinedEntityData<?> mobends$bent() {
        return mobends$bent;
    }

    @Override
    @Nullable
    public EntityModel<?> mobends$model() {
        return mobends$model;
    }

    @Override
    public void mobends$setBent(@Nullable DefinedEntityData<?> bent, @Nullable EntityModel<?> model) {
        mobends$bent = bent;
        mobends$model = model;
        mobends$layerFrames.clear();
    }

    @Override
    public Matrix4f mobends$entityFrame() {
        return mobends$entityFrame;
    }

    @Override
    public float mobends$bodyYaw() {
        return mobends$bodyYaw;
    }

    @Override
    public void mobends$setBodyYaw(float bodyYaw) {
        mobends$bodyYaw = bodyYaw;
    }

    @Override
    public Map<Object, BendsLayers.Frame> mobends$layerFrames() {
        return mobends$layerFrames;
    }
}
*///?}
