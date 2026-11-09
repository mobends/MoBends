package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.vanilla.AgeableModelLayout;
import goblinbob.mobends.core.vanilla.ModelRootHolder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The parts Mo' Bends draws for one vanilla model instance (a player's default-armed model and its
 * slim-armed one are two), built from a model definition by {@link DefinedModelBuilder} the first
 * time the model is drawn bent, and kept until the resource packs reload. A model worn over an
 * entity's model (the vanilla armour) gets parts of its own too ({@link #over}).
 */
public final class DefinedModel {
    /** By vanilla model, which the game keeps as long as its renderer; the models are made again on a reload. */
    private static final Map<EntityModel<?>, DefinedModel> CACHE = new WeakHashMap<>();
    /** The same, for the models worn over another ({@link #over}). */
    private static final Map<EntityModel<?>, DefinedModel> WORN_CACHE = new WeakHashMap<>();
    /** A model that can't be built (it doesn't keep its root), so it is drawn as vanilla, said once. */
    private static final DefinedModel NONE = new DefinedModel(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(),
            Collections.emptyMap(), Collections.emptyMap(), null);

    private final List<DefinedModelPart> roots;
    /** Every bone, segment and overlay by name (an overlay by its first piece). */
    private final Map<String, DefinedModelPart> parts;
    private final Map<String, float[]> positions;
    /** Per bone: how far its boxes moved into its frame, which a model worn over this one follows. */
    final Map<String, float[]> shifts;
    /** Per bone: its segments' origins in its frame, where a model worn over this one is cut. */
    final Map<String, List<float[]>> segmentOrigins;
    /** The parts this model is worn over; null for an entity's own. */
    @Nullable
    private final DefinedModel wornOver;
    /** How a baby is drawn with these parts; null if the model draws babies as adults. */
    @Nullable
    private BabyLayout babyLayout;

    DefinedModel(List<DefinedModelPart> roots, Map<String, DefinedModelPart> parts, Map<String, float[]> positions,
                 Map<String, float[]> shifts, Map<String, List<float[]>> segmentOrigins, @Nullable DefinedModel wornOver) {
        this.roots = roots;
        this.parts = parts;
        this.positions = positions;
        this.shifts = shifts;
        this.segmentOrigins = segmentOrigins;
        this.wornOver = wornOver;
    }

    /** The parts for {@code model}, built now if they weren't; null if the model can't be drawn bent. */
    @Nullable
    public static DefinedModel of(EntityModelDefinition definition, EntityModel<?> model) {
        DefinedModel built = CACHE.get(model);
        if (built == null) {
            ModelPart root = rootOf(definition, model);
            built = root == null ? NONE : DefinedModelBuilder.build(definition, root);
            if (built != NONE && model instanceof AgeableModelLayout layout) {
                built.babyLayout = BabyLayout.of(layout, built.roots);
            }
            CACHE.put(model, built);
        }
        return built == NONE ? null : built;
    }

    /**
     * The parts for {@code model} worn over {@code body} (a vanilla armour model over the player's),
     * built now if they weren't, or were for another body; null if the model can't be drawn bent.
     * They are posed by the body's data, cut at the body's joints (see {@link DefinedModelBuilder#buildOver}).
     */
    @Nullable
    public static DefinedModel over(EntityModelDefinition definition, EntityModel<?> model, DefinedModel body) {
        DefinedModel built = WORN_CACHE.get(model);
        if (built == null || (built != NONE && built.wornOver != body)) {
            ModelPart root = rootOf(definition, model);
            built = root == null ? NONE : DefinedModelBuilder.buildOver(definition, root, body);
            WORN_CACHE.put(model, built);
        }
        return built == NONE ? null : built;
    }

    @Nullable
    private static ModelPart rootOf(EntityModelDefinition definition, EntityModel<?> model) {
        ModelPart root = model instanceof ModelRootHolder holder ? holder.mobends$getRoot()
                : model instanceof HierarchicalModel<?> hierarchical ? hierarchical.root() : null;
        if (root == null) {
            MoBends.LOGGER.warn("Model definition {}: can't read the parts of {}; it is drawn as vanilla draws it.",
                    definition.location, model.getClass().getName());
        }
        return root;
    }

    /** Forgets every built model: the definitions they came from are read again. */
    public static void clearCache() {
        CACHE.clear();
        WORN_CACHE.clear();
    }

    /** Whether the model has a part named {@code name} (a bone, a segment or an overlay). */
    public boolean hasPart(String name) {
        return parts.containsKey(name);
    }

    /**
     * Moves {@code poseStack}, at the model's space as the bent parts are drawn in it, into the
     * frame vanilla's part for bone {@code name} has, posed as the bone is: through the bone's
     * parents, the bone and its further segments (see {@link DefinedModelPart#applyVanillaFrame}).
     * What vanilla draws attached to its part (a held item, a hat) is drawn there.
     *
     * @param nudgeX how far vanilla moves its part before turning it, in model units (usually 0)
     * @return false if there is no such bone, and nothing was done
     */
    public boolean applyVanillaFrame(String name, DefinedEntityData<?> data, PoseStack poseStack, float nudgeX, float nudgeY, float nudgeZ) {
        DefinedModelPart part = parts.get(name);
        if (part == null) {
            return false;
        }
        part.applyVanillaFrame(poseStack, data, nudgeX, nudgeY, nudgeZ);
        return true;
    }

    /**
     * Applies part {@code name}'s own transform alone, in its parent's frame, as it is drawn.
     *
     * @return false if there is no such part, and nothing was done
     */
    public boolean applyLocalTransform(String name, DefinedEntityData<?> data, PoseStack poseStack) {
        DefinedModelPart part = parts.get(name);
        if (part == null) {
            return false;
        }
        part.applyLocalTransform(poseStack, data);
        return true;
    }

    /**
     * Where each bone and segment sits in its parent's frame, read off the vanilla model, for the
     * entity's data to adopt ({@link DefinedEntityData#adoptPositions}).
     */
    public Map<String, float[]> positions() {
        return positions;
    }

    /**
     * How a baby is drawn with these parts (with a model worn over them too), or null to draw it
     * as an adult; see {@link #render}.
     */
    @Nullable
    public BabyLayout babyLayout() {
        return babyLayout;
    }

    /**
     * The name of the part bone {@code name} is drawn under as a root of the model (itself, if
     * it's one), whose baby transform it is drawn with; null if there's no such bone.
     */
    @Nullable
    public String rootPartOf(String name) {
        DefinedModelPart part = parts.get(name);
        while (part != null && !roots.contains(part)) {
            part = part.parent;
        }
        return part == null ? null : part.name;
    }

    /**
     * Draws the parts posed as the data has them, in the model's space (as {@code renderToBuffer}
     * would): a baby (with {@code baby}) as vanilla draws its model's parts.
     */
    public void render(DefinedEntityData<?> data, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color,
                       @Nullable BabyLayout baby) {
        for (DefinedModelPart part : roots) {
            if (baby != null) {
                poseStack.pushPose();
                baby.apply(part.name, poseStack);
            }
            part.renderAsRoot(poseStack, consumer, packedLight, packedOverlay, color, data);
            if (baby != null) {
                poseStack.popPose();
            }
        }
    }

    /**
     * Draws bone {@code name} alone, its segments with it, all at rest, in the frame of vanilla's
     * part for it as {@code vanillaPart} is posed: the bone's own boxes, or with {@code overlay},
     * only the pieces of that overlay bone laid over it (a sleeve). The first-person hand: vanilla's
     * arm, posed as vanilla poses it, with the bent arm's boxes.
     *
     * @return false if there is no such bone, and nothing was drawn
     */
    public boolean renderAlone(String name, @Nullable String overlay, ModelPart vanillaPart, DefinedEntityData<?> data, PoseStack poseStack,
                               VertexConsumer consumer, int packedLight, int packedOverlay) {
        DefinedModelPart part = parts.get(name);
        if (part == null) {
            return false;
        }
        DefinedModelPart.allAtRest = true;
        DefinedModelPart.drawn = overlay == null ? p -> p.overlayOf == null : p -> overlay.equals(p.overlayOf);
        poseStack.pushPose();
        try {
            vanillaPart.translateAndRotate(poseStack);
            part.renderInVanillaFrame(poseStack, consumer, packedLight, packedOverlay, -1, data);
        } finally {
            poseStack.popPose();
            DefinedModelPart.allAtRest = false;
            DefinedModelPart.drawn = null;
        }
        return true;
    }

    /**
     * What vanilla's {@code AgeableListModel.renderToBuffer} does to a baby's parts, per root part
     * of the bent model: a root standing for one of the model's head parts is drawn as the head,
     * any other as the body (the bones under it with it). A zombie's head hangs from its body, so a
     * bent baby zombie is the bent adult at half size, as 1.12 drew it.
     */
    public record BabyLayout(Set<String> headRoots, boolean scalesHead, float headY, float headZ, float headScale, float bodyScale, float bodyY) {
        static BabyLayout of(AgeableModelLayout layout, List<DefinedModelPart> roots) {
            Set<ModelPart> headParts = Collections.newSetFromMap(new IdentityHashMap<>());
            layout.mobends$headParts().forEach(headParts::add);
            Set<String> headRoots = new HashSet<>();
            for (DefinedModelPart root : roots) {
                if (root.name != null && root.visibleWith != null && headParts.contains(root.visibleWith)) {
                    headRoots.add(root.name);
                }
            }
            return new BabyLayout(headRoots, layout.mobends$scalesHead(), layout.mobends$babyYHeadOffset(), layout.mobends$babyZHeadOffset(),
                    layout.mobends$babyHeadScale(), layout.mobends$babyBodyScale(), layout.mobends$bodyYOffset());
        }

        /** Applies the transform of the root part {@code root} (null: the body's). */
        public void apply(@Nullable String root, PoseStack poseStack) {
            if (root != null && headRoots.contains(root)) {
                if (scalesHead) {
                    float scale = 1.5F / headScale;
                    poseStack.scale(scale, scale, scale);
                }
                poseStack.translate(0.0F, headY / 16.0F, headZ / 16.0F);
            } else {
                float scale = 1.0F / bodyScale;
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0F, bodyY / 16.0F, 0.0F);
            }
        }
    }
}
