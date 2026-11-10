package goblinbob.mobends.core.client;

import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.EntityModel;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * What an entity's render state carries for Mo' Bends on 26.x, where an entity is drawn from its
 * render state, later in the frame than its renderer runs ({@code EntityRenderStateMixin}): the
 * data it's bent by, and what drawing it bent needs later on.
 */
public interface BentRenderState {
    /** The data the entity is bent by this frame, or null if it's drawn as vanilla. */
    @Nullable
    DefinedEntityData<?> mobends$bent();

    /** The renderer's model the data was made for. */
    @Nullable
    EntityModel<?> mobends$model();

    /** Set as the state is filled from the entity: the data, and the model it's for. */
    void mobends$setBent(@Nullable DefinedEntityData<?> bent, @Nullable EntityModel<?> model);

    /** The pose as the renderer's {@code setupRotations} starts (set as the entity is submitted). */
    Matrix4f mobends$entityFrame();

    /** The body yaw (degrees) the renderer turns the entity by. */
    float mobends$bodyYaw();

    void mobends$setBodyYaw(float bodyYaw);

    /** Each model a layer submitted to be drawn bent, by its submit, to the frame it's drawn in ({@code BendsLayers#frameForSubmit}). */
    Map<Object, BendsLayers.Frame> mobends$layerFrames();
}
