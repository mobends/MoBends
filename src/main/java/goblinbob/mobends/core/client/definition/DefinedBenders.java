package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.DefinedFields;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.vanilla.VanillaEntityFields;
import goblinbob.mobends.core.vanilla.VanillaModelParts;
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
        DefinedFields.install(VanillaEntityFields::get, VanillaModelParts::get);
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

    private static void register(String modId, AddonAnimationRegistry registry, EntityModelDefinition definition) throws ClassNotFoundException
    {
        registry.registerEntity(createBender(modId, definition));
    }

    /** The bender of a model definition, not registered yet. Its key is prefixed with {@code modId}. */
    @SuppressWarnings("unchecked")
    public static <E extends EntityLivingBase> EntityBender<E> createBender(String modId, EntityModelDefinition definition) throws ClassNotFoundException
    {
        Class<E> entityClass = (Class<E>) Class.forName(definition.entity);
        return new DefaultEntityBender<>(modId, definition.key, definition.unlocalizedName, entityClass,
                entity -> DefinedEntityData.create(definition, entity),
                () -> new DefinedMutator<>(definition),
                new DefinedRenderer<>(definition),
                definition.alterablePartsOrAll());
    }

}
