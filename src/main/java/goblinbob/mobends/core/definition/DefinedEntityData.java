package goblinbob.mobends.core.definition;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.KumoAnimatorController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

/**
 * The entity data of a mob described by an {@link EntityModelDefinition}: one transform per bone
 * (split segments included), the definition's entity-field variables, and the animator as the
 * controller. Positions default to the definition's; the mutator hands over the vanilla rotation
 * points and split pivots the first time it syncs.
 */
public class DefinedEntityData<E extends EntityLivingBase> extends LivingEntityData<E>
{

    /** The definition is needed inside {@code initModelPose}, which the base constructor calls. */
    private static final ThreadLocal<EntityModelDefinition> CONSTRUCTING = new ThreadLocal<>();

    private final EntityModelDefinition definition;
    /** Filled by {@code initModelPose}, which the base constructor calls before field initialisers run. */
    private Map<String, ModelPartTransform> parts;
    private final KumoAnimatorController<DefinedEntityData<E>> controller;
    private boolean positionsAdopted;

    public static <E extends EntityLivingBase> DefinedEntityData<E> create(EntityModelDefinition definition, E entity)
    {
        CONSTRUCTING.set(definition);
        try
        {
            return new DefinedEntityData<>(definition, entity);
        }
        finally
        {
            CONSTRUCTING.remove();
        }
    }

    protected DefinedEntityData(EntityModelDefinition definition, E entity)
    {
        super(entity);
        this.definition = definition;
        this.controller = new KumoAnimatorController<>(new ResourceLocation(definition.animator));
        for (VariableDefinition variable : definition.variables)
        {
            registerVariable(variable.name, supplierFor(variable));
        }
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
        EntityModelDefinition definition = CONSTRUCTING.get();
        if (parts == null)
        {
            parts = new LinkedHashMap<>();
        }
        if (definition == null)
        {
            return;
        }
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
                parts.put(names.get(i), part);
                nameToPartMap.put(names.get(i), part);
                previous = part;
            }
        }
    }

    /** Called once by the mutator with the positions read off the vanilla model (and the split pivots). */
    public void adoptPositions(Map<String, float[]> positions)
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
        positionsAdopted = true;
    }

    public boolean hasAdoptedPositions()
    {
        return positionsAdopted;
    }

    @Override
    public IAnimationController<?> getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
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

    // --- entity fields as variables ---------------------------------------------------------------

    private DoubleSupplier supplierFor(VariableDefinition variable)
    {
        if (variable.product != null && variable.product.size() >= 2)
        {
            return () -> {
                double value = 1;
                for (String name : variable.product)
                {
                    value *= getVariable(name);
                }
                return shape(variable, value);
            };
        }
        ToDoubleFunction<Object> current = entity == null ? null : DefinedFields.number(entity.getClass(), variable.field);
        ToDoubleFunction<Object> previous = entity == null || variable.prevField == null ? null : DefinedFields.number(entity.getClass(), variable.prevField);
        if (current == null)
        {
            return () -> variable.offset;
        }
        return () -> {
            double now = current.applyAsDouble(entity);
            if (previous != null)
            {
                double before = previous.applyAsDouble(entity);
                now = before + (now - before) * DataUpdateHandler.partialTicks;
            }
            return shape(variable, now);
        };
    }

    private static double shape(VariableDefinition variable, double raw)
    {
        double value = raw * variable.scale + variable.offset;
        if (variable.fn != null)
        {
            switch (variable.fn.toLowerCase())
            {
                case "sin": value = Math.sin(value); break;
                case "cos": value = Math.cos(value); break;
                case "mcsin": value = net.minecraft.util.math.MathHelper.sin((float) value); break;
                case "mccos": value = net.minecraft.util.math.MathHelper.cos((float) value); break;
                case "abs": value = Math.abs(value); break;
                default: break;
            }
        }
        return value + variable.add;
    }

}
