package goblinbob.mobends.core.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.bind.EntityBone;
import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The entity data of a mob described by an {@link EntityModelDefinition}: one transform per bone
 * (split segments included), the definition's entity scope, and its animator. Positions default to
 * the definition's; whoever builds the drawn parts hands over the vanilla rotation points and split
 * pivots ({@link #adoptPositions}).
 */
public class DefinedEntityData<E extends LivingEntity> extends LivingEntityData<E> {
    /** The definition and component kind pairs already warned about, so a missing kind is told once, not per entity. */
    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<>());

    private final EntityModelDefinition definition;
    private final Map<String, ModelPartTransform> parts = new LinkedHashMap<>();
    private final ResourceLocation animator;
    /** What the positions were last adopted from (one per vanilla model), or null. */
    private Object positionsSource;
    /** The vanilla model's pose, which a {@code core:vanilla_pose} node reads; null while there's none to read. */
    @Nullable
    private IVanillaPose vanillaPose;
    /** Whatever the renderer keeps to read it with, from one frame to the next (see {@link #getVanillaPoseSource}). */
    @Nullable
    private Object vanillaPoseSource;
    /** How much this frame's animation gives the entity vanilla's frame (see {@link #getEntityFrameVanilla}). */
    private float entityFrameVanilla;

    public DefinedEntityData(EntityModelDefinition definition, E entity) {
        super(entity);
        this.definition = definition;
        this.animator = Ids.parse(definition.animator);
    }

    public EntityModelDefinition getDefinition() {
        return definition;
    }

    public ModelPartTransform getPart(String name) {
        return parts.get(name);
    }

    /** Every bone's transform by name, split segments included, in declaration order. */
    public Map<String, ModelPartTransform> getParts() {
        return parts;
    }

    @Override
    public void initModelPose() {
        super.initModelPose();
        for (BoneDefinition bone : definition.bones) {
            ModelPartTransform parent = bone.parent == null ? null : parts.get(bone.parent);
            ModelPartTransform previous = null;
            List<String> names = bone.segmentNames();
            for (int i = 0; i < names.size(); i++) {
                // A split bone's segments hang one from the other, the first from the bone's parent.
                ModelPartTransform part = new ModelPartTransform(i == 0 ? parent : previous);
                if (i == 0 && bone.position != null) {
                    part.position.set(bone.position[0], bone.position[1], bone.position[2]);
                }
                if (i == 0) {
                    part.inheritRotation = bone.inheritRotation;
                }
                parts.put(names.get(i), part);
                nameToPartMap.put(names.get(i), part);
                previous = part;
            }
        }
        setAttackComboTicks(definition.attackComboTicks);
        if (definition.components != null) {
            for (Map.Entry<String, String> component : definition.components.entrySet()) {
                EntityComponents.Factory factory = EntityComponents.get(component.getValue());
                if (factory == null) {
                    // The animator still loads: a driver that feeds a missing component does nothing.
                    if (WARNED.add(definition.location + " " + component.getValue())) {
                        MoBends.LOGGER.warn("Model definition {}: there is no component '{}' (for '{}'); it is left out.",
                                definition.location, component.getValue(), component.getKey());
                    }
                    continue;
                }
                addComponent(component.getKey(), factory.create(this));
            }
        }
        if (definition.smoothing != null) {
            for (Map.Entry<String, BoneSmoothing> entry : definition.smoothing.entrySet()) {
                if (!setSmoothing(entry.getKey(), entry.getValue()) && WARNED.add(definition.location + " smoothing " + entry.getKey())) {
                    MoBends.LOGGER.warn("Model definition {}: 'smoothing' names '{}', which is nothing that turns or moves.",
                            definition.location, entry.getKey());
                }
            }
        }
    }

    private boolean setSmoothing(String name, BoneSmoothing smoothing) {
        Object bone = getBone(name);
        if (bone instanceof EntityBone entityBone) {
            if (smoothing.rotation != null) {
                entityBone.rotation.setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            if (smoothing.position != null) {
                entityBone.position.smoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
                entityBone.position.baseSmoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
            }
            return true;
        }
        Object part = getPartForName(name);
        if (part instanceof ModelPartTransform transform) {
            if (smoothing.rotation != null) {
                transform.rotation.setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            if (smoothing.position != null) {
                transform.smoothOffset.baseSmoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
            }
            return true;
        }
        if (part instanceof SmoothOrientation orientation) {
            if (smoothing.rotation != null) {
                orientation.setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            return true;
        }
        return false;
    }

    @Override
    public Vec3f getCenterPivot(Vec3f dest) {
        EntityModelDefinition.CenterSettings center = definition.center;
        if (center == null || center.pivot == null) {
            return super.getCenterPivot(dest);
        }
        // Model units to blocks, as the renderer scales the model (a baby's by its childScale).
        float scale = 0.0625F * (entity.isBaby() ? definition.childScale : 1F);
        dest.set(center.pivot[0] * scale, center.pivot[1] * scale, center.pivot[2] * scale);
        return dest;
    }

    /**
     * Takes the positions read off a vanilla model (its rotation points, and the split pivots):
     * once, and again whenever the entity is drawn with another model ({@code source}).
     */
    public void adoptPositions(Object source, Map<String, float[]> positions) {
        for (Map.Entry<String, float[]> entry : positions.entrySet()) {
            ModelPartTransform part = parts.get(entry.getKey());
            BoneDefinition bone = definition.bone(entry.getKey());
            // A declared position wins over the vanilla rotation point.
            if (part != null && (bone == null || bone.position == null)) {
                float[] p = entry.getValue();
                part.position.set(p[0], p[1], p[2]);
            }
        }
        positionsSource = source;
    }

    @Override
    @Nullable
    public IVanillaPose getVanillaPose() {
        return vanillaPose;
    }

    /** Gives the animator the vanilla model's pose to read, while it animates; null for none. */
    public void setVanillaPose(@Nullable IVanillaPose vanillaPose) {
        this.vanillaPose = vanillaPose;
    }

    /** What the renderer reads the vanilla pose with, kept with the entity's data; null until it sets one. */
    @Nullable
    public Object getVanillaPoseSource() {
        return vanillaPoseSource;
    }

    public void setVanillaPoseSource(@Nullable Object source) {
        this.vanillaPoseSource = source;
    }

    /**
     * How much this frame's animation gives the entity vanilla's frame, 0..1: 1 where a
     * {@code core:vanilla_pose} node gave {@code root} and {@code center} vanilla's frame, in between
     * while it fades in or out. The bones carry the frame itself
     * ({@code DefinedRenderer.vanillaFrameAsRoot}); what Mo' Bends draws of its own that vanilla
     * doesn't (the sword trail) fades out by as much.
     */
    public float getEntityFrameVanilla() {
        return entityFrameVanilla;
    }

    @Override
    public void setEntityFrameVanilla(float weight) {
        this.entityFrameVanilla = weight;
    }

    @Override
    protected void beforeAnimate() {
        super.beforeAnimate();
        // Set again by this frame's animation (the animator says it every frame it runs); kept
        // through any draw after it in the same frame, which doesn't animate again.
        entityFrameVanilla = 0F;
    }

    public boolean hasAdoptedPositionsOf(Object source) {
        return positionsSource == source;
    }

    @Override
    public void update(float partialTicks) {
        super.update(partialTicks);
        if (definition.childBones != null) {
            boolean child = entity.isBaby();
            for (Map.Entry<String, EntityModelDefinition.ChildBone> entry : definition.childBones.entrySet()) {
                ModelPartTransform part = parts.get(entry.getKey());
                float[] offset = child ? entry.getValue().globalOffset : null;
                part.offsetScale = child ? entry.getValue().offsetScale : 1;
                part.globalOffset.set(offset != null ? offset[0] : 0, offset != null ? offset[1] : 0, offset != null ? offset[2] : 0);
            }
        }
    }

    @Override
    protected ResourceLocation getDefaultAnimator() {
        return animator;
    }

    @Override
    public EntityTemplate getEntityScope() {
        return definition.entityScope(entity.getClass());
    }

    @Override
    public void updateParts(float ticksPerFrame) {
        super.updateParts(ticksPerFrame);
        for (ModelPartTransform part : parts.values()) {
            part.update(ticksPerFrame);
        }
    }
}
