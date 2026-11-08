package goblinbob.mobends.core.client;

import goblinbob.mobends.core.definition.DefinedEntityData;

import javax.annotation.Nullable;

/**
 * Every {@code LivingEntityRenderer} (through {@code mixin.LivingEntityRendererMixin}): what the
 * renderer's subclasses' mixins need to know about the entity it's drawing.
 */
public interface BendingEntityRenderer {
    /** The data of the entity being drawn, posed for this frame; null while it's drawn as vanilla does. */
    @Nullable
    DefinedEntityData<?> mobends$bentData();
}
