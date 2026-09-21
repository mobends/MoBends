package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import net.minecraft.entity.EntityLivingBase;

import java.util.logging.Level;

/** Registers an animated entity for every model definition listed in {@code bends/models/index.json}. */
public final class DefinedBenders
{

    private DefinedBenders()
    {
    }

    public static void registerAll(String modId, AddonAnimationRegistry registry)
    {
        try
        {
            for (String name : ModelDefinitions.INSTANCE.index(modId))
            {
                try
                {
                    register(modId, registry, ModelDefinitions.INSTANCE.load(modId, name));
                }
                catch (Exception e)
                {
                    Core.LOG.log(Level.SEVERE, "Could not register the model definition '" + name + "'", e);
                }
            }
        }
        catch (Exception e)
        {
            Core.LOG.log(Level.SEVERE, "Could not read the model definition index of " + modId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends EntityLivingBase> void register(String modId, AddonAnimationRegistry registry, EntityModelDefinition definition) throws ClassNotFoundException
    {
        Class<E> entityClass = (Class<E>) Class.forName(definition.entity);
        registry.registerNewEntity(definition.key, definition.unlocalizedName, entityClass,
                entity -> DefinedEntityData.create(definition, entity),
                dataFactory -> new DefinedMutator<>(definition, dataFactory),
                new DefinedRenderer<>(definition),
                definition.alterablePartsOrAll());
    }

}
