package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.ModelState;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

/**
 * A vanilla layer that draws its own copy of the mob's model (the sheep's wool, a charged
 * creeper's armour), drawn animated: the copy is mutated by the mob's model definition, as the
 * main model is, cut where the main model's segments are, and posed from the entity's data before
 * the vanilla layer draws it. The copy is mutated only while this layer draws it: the vanilla layer,
 * drawing a vanilla mob, draws the vanilla copy.
 */
public class AnimatedCopyLayer implements LayerRenderer<EntityLivingBase>
{

    private final LayerRenderer<EntityLivingBase> vanilla;
    private final DefinedMutator<EntityLivingBase> mutator;
    /** The copy as vanilla has it, and as mutated; null when it isn't animated. */
    private final ModelState vanillaCopy;
    private final ModelState mutatedCopy;

    @SuppressWarnings("unchecked")
    public AnimatedCopyLayer(DefinedLayers.Context context)
    {
        this.vanilla = (LayerRenderer<EntityLivingBase>) context.replaced;
        ModelBase copy = copyOf(context.replaced, context.renderer.getMainModel());
        DefinedMutator<EntityLivingBase> mutator = new DefinedMutator<>(context.definition, context.segmentOrigins);
        if (copy == null || mutator.shouldModelBeSkipped(copy))
        {
            Core.LOG.log(Level.WARNING, "Model definition for " + context.definition.entity + ": the layer " + context.replaced + " has no copy of the model to animate.");
            this.mutator = null;
            this.vanillaCopy = null;
            this.mutatedCopy = null;
        }
        else
        {
            // The copy is vanilla here: every layer made for it before put it back after mutating it.
            this.vanillaCopy = ModelState.capture(copy);
            mutator.createParts(copy);
            this.mutatedCopy = ModelState.capture(copy);
            this.vanillaCopy.apply();
            this.mutator = mutator;
        }
    }

    /** The model the layer draws: a field of its holding a model other than the renderer's own. */
    private static ModelBase copyOf(Object layer, ModelBase main)
    {
        for (Class<?> c = layer == null ? null : layer.getClass(); c != null && c != Object.class; c = c.getSuperclass())
        {
            for (Field field : c.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) || !ModelBase.class.isAssignableFrom(field.getType()))
                {
                    continue;
                }
                try
                {
                    field.setAccessible(true);
                    Object value = field.get(layer);
                    if (value != null && value != main)
                    {
                        return (ModelBase) value;
                    }
                }
                catch (IllegalAccessException | SecurityException ignored)
                {
                    // A field that can't be read isn't the copy we can animate.
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                              float netHeadYaw, float headPitch, float scale)
    {
        EntityData<?> data = EntityDatabase.instance.get(entity);
        if (mutator == null || !(data instanceof DefinedEntityData))
        {
            vanilla.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }
        mutatedCopy.apply();
        try
        {
            mutator.syncUpParts((DefinedEntityData<EntityLivingBase>) data);
            vanilla.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }
        finally
        {
            vanillaCopy.apply();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return vanilla.shouldCombineTextures();
    }

}
