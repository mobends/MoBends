package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.bind.EntityBone;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The entity data of a mob described by an {@link EntityModelDefinition}: one transform per bone
 * (split segments included), the definition's entity scope, and its animator.
 * Positions default to the definition's; the mutator hands over the vanilla rotation points and
 * split pivots the first time it syncs.
 */
public class DefinedEntityData<E extends EntityLivingBase> extends LivingEntityData<E>
{

    private final EntityModelDefinition definition;
    private final Map<String, ModelPartTransform> parts = new LinkedHashMap<>();
    private final ResourceLocation animator;
    /** What the positions were last adopted from (a mutator, one per renderer), or null. */
    private Object positionsSource;

    public static <E extends EntityLivingBase> DefinedEntityData<E> create(EntityModelDefinition definition, E entity)
    {
        DefinedEntityData<E> data = new DefinedEntityData<>(definition, entity);
        data.initialize();
        return data;
    }

    protected DefinedEntityData(EntityModelDefinition definition, E entity)
    {
        super(entity);
        this.definition = definition;
        this.animator = new ResourceLocation(definition.animator);
    }

    public EntityModelDefinition getDefinition()
    {
        return definition;
    }

    public ModelPartTransform getPart(String name)
    {
        return parts.get(name);
    }

    public Map<String, ModelPartTransform> getParts()
    {
        return parts;
    }

    @Override
    public void initModelPose()
    {
        super.initModelPose();
        for (BoneDefinition bone : definition.bones)
        {
            ModelPartTransform parent = bone.parent == null ? null : parts.get(bone.parent);
            ModelPartTransform previous = null;
            List<String> names = bone.segmentNames();
            for (int i = 0; i < names.size(); i++)
            {
                ModelPartTransform part = new ModelPartTransform(i == 0 ? parent : previous);
                if (i == 0 && bone.position != null)
                {
                    part.position.set(bone.position[0], bone.position[1], bone.position[2]);
                }
                if (i == 0)
                {
                    part.inheritRotation = bone.inheritRotation;
                }
                parts.put(names.get(i), part);
                nameToPartMap.put(names.get(i), part);
                previous = part;
            }
        }
        setAttackComboTicks(definition.attackComboTicks);
        if (definition.components != null)
        {
            for (Map.Entry<String, String> component : definition.components.entrySet())
            {
                EntityComponents.Factory factory = EntityComponents.get(component.getValue());
                if (factory == null)
                {
                    Core.LOG.warning("Model definition for " + definition.entity + ": there is no component '" + component.getValue() + "' (for '" + component.getKey() + "').");
                    continue;
                }
                addComponent(component.getKey(), factory.create(this));
            }
        }
        if (definition.smoothing != null)
        {
            for (Map.Entry<String, BoneSmoothing> entry : definition.smoothing.entrySet())
            {
                if (!setSmoothing(entry.getKey(), entry.getValue()))
                {
                    Core.LOG.warning("Model definition for " + definition.entity + ": 'smoothing' names '" + entry.getKey() + "', which is nothing that turns or moves.");
                }
            }
        }
    }

    private boolean setSmoothing(String name, BoneSmoothing smoothing)
    {
        Object bone = getBone(name);
        if (bone instanceof EntityBone)
        {
            EntityBone entityBone = (EntityBone) bone;
            if (smoothing.rotation != null)
            {
                entityBone.rotation.setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            if (smoothing.position != null)
            {
                entityBone.position.smoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
                entityBone.position.baseSmoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
            }
            return true;
        }
        Object part = getPartForName(name);
        if (part instanceof ModelPartTransform)
        {
            ModelPartTransform transform = (ModelPartTransform) part;
            if (smoothing.rotation != null)
            {
                transform.rotation.setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            if (smoothing.position != null)
            {
                transform.smoothOffset.baseSmoothing.set(smoothing.position[0], smoothing.position[1], smoothing.position[2]);
            }
            return true;
        }
        if (part instanceof SmoothOrientation)
        {
            if (smoothing.rotation != null)
            {
                ((SmoothOrientation) part).setSmoothing(smoothing.rotation).setBaseSmoothing(smoothing.rotation);
            }
            return true;
        }
        return false;
    }

    @Override
    public Vec3f getCenterPivot(Vec3f dest)
    {
        EntityModelDefinition.CenterSettings center = definition.center;
        if (center == null || center.pivot == null)
        {
            return super.getCenterPivot(dest);
        }
        // Model units to blocks, as the renderer scales the model (a baby's by its childScale).
        float scale = 0.0625F * (entity != null && entity.isChild() ? definition.childScale : 1F);
        dest.set(center.pivot[0] * scale, center.pivot[1] * scale, center.pivot[2] * scale);
        return dest;
    }

    /**
     * Called by a mutator with the positions read off its renderer's vanilla model (and the split
     * pivots): once, and again whenever the entity is drawn by another renderer.
     */
    public void adoptPositions(Object source, Map<String, float[]> positions)
    {
        for (Map.Entry<String, float[]> entry : positions.entrySet())
        {
            ModelPartTransform part = parts.get(entry.getKey());
            BoneDefinition bone = definition.bone(entry.getKey());
            // A declared position wins over the vanilla rotation point.
            if (part != null && (bone == null || bone.position == null))
            {
                float[] p = entry.getValue();
                part.position.set(p[0], p[1], p[2]);
            }
        }
        positionsSource = source;
    }

    public boolean hasAdoptedPositionsOf(Object source)
    {
        return positionsSource == source;
    }

    @Override
    public void update(float partialTicks)
    {
        super.update(partialTicks);
        if (definition.childBones != null && entity != null)
        {
            boolean child = entity.isChild();
            for (Map.Entry<String, EntityModelDefinition.ChildBone> entry : definition.childBones.entrySet())
            {
                ModelPartTransform part = parts.get(entry.getKey());
                float[] offset = entry.getValue().globalOffset;
                part.offsetScale = child ? entry.getValue().offsetScale : 1;
                part.globalOffset.set(child && offset != null ? offset[0] : 0, child && offset != null ? offset[1] : 0, child && offset != null ? offset[2] : 0);
            }
        }
    }

    @Override
    protected ResourceLocation getDefaultAnimator()
    {
        return animator;
    }

    @Override
    public EntityTemplate getEntityScope()
    {
        return entity == null ? null : definition.entityScope(entity.getClass());
    }

    @Override
    public void updateParts(float ticksPerFrame)
    {
        super.updateParts(ticksPerFrame);
        for (ModelPartTransform part : parts.values())
        {
            part.update(ticksPerFrame);
        }
    }

}
