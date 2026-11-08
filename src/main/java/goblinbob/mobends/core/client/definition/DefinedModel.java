package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.vanilla.ModelRootHolder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The parts Mo' Bends draws for one vanilla model instance (a player's default-armed model and its
 * slim-armed one are two), built from a model definition by {@link DefinedModelBuilder} the first
 * time the model is drawn bent, and kept until the resource packs reload.
 */
public final class DefinedModel {
    /** By vanilla model, which the game keeps as long as its renderer; the models are made again on a reload. */
    private static final Map<EntityModel<?>, DefinedModel> CACHE = new WeakHashMap<>();
    /** A model that can't be built (it doesn't keep its root), so it is drawn as vanilla, said once. */
    private static final DefinedModel NONE = new DefinedModel(Collections.emptyList(), Collections.emptyMap());

    private final List<DefinedModelPart> roots;
    private final Map<String, float[]> positions;

    DefinedModel(List<DefinedModelPart> roots, Map<String, float[]> positions) {
        this.roots = roots;
        this.positions = positions;
    }

    /** The parts for {@code model}, built now if they weren't; null if the model can't be drawn bent. */
    @Nullable
    public static DefinedModel of(EntityModelDefinition definition, EntityModel<?> model) {
        DefinedModel built = CACHE.get(model);
        if (built == null) {
            ModelPart root = model instanceof ModelRootHolder holder ? holder.mobends$getRoot() : null;
            if (root == null) {
                MoBends.LOGGER.warn("Model definition {}: can't read the parts of {}; it is drawn as vanilla draws it.",
                        definition.location, model.getClass().getName());
                built = NONE;
            } else {
                built = DefinedModelBuilder.build(definition, root);
            }
            CACHE.put(model, built);
        }
        return built == NONE ? null : built;
    }

    /** Forgets every built model: the definitions they came from are read again. */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Where each bone and segment sits in its parent's frame, read off the vanilla model, for the
     * entity's data to adopt ({@link DefinedEntityData#adoptPositions}).
     */
    public Map<String, float[]> positions() {
        return positions;
    }

    /** Draws the parts posed as the data has them, in the model's space (as {@code renderToBuffer} would). */
    public void render(DefinedEntityData<?> data, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
        for (DefinedModelPart part : roots) {
            part.renderAsRoot(poseStack, consumer, packedLight, packedOverlay, color, data);
        }
    }
}
