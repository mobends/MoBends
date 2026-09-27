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
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;
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
                    ResourceLocation location = ModelDefinitions.locationOf(modId, name);
                    registry.registerEntity(createBender(modId, location, ModelDefinitions.INSTANCE.load(location)));
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

    /**
     * The bender of the model definition at {@code location} ({@code definition} is it as loaded
     * now), not registered yet. Its key is prefixed with {@code modId}. The entity's data, model and
     * renderer read the definition again whenever they are made (after a reload, or when the server
     * changes what resource packs may do), so the geometry always comes from where it's allowed to.
     */
    @SuppressWarnings("unchecked")
    public static <E extends EntityLivingBase> EntityBender<E> createBender(String modId, ResourceLocation location, EntityModelDefinition definition) throws ClassNotFoundException
    {
        Class<E> entityClass = (Class<E>) Class.forName(definition.entity);
        Supplier<EntityModelDefinition> current = () -> {
            try
            {
                return ModelDefinitions.INSTANCE.load(location);
            }
            catch (Exception e)
            {
                Core.LOG.log(Level.WARNING, "Could not load the model definition " + location + " again, keeping the one loaded first: " + e.getMessage());
                return definition;
            }
        };
        return new DefaultEntityBender<>(modId, definition.key, definition.unlocalizedName, entityClass,
                entity -> DefinedEntityData.create(current.get(), entity),
                () -> new DefinedMutator<>(current.get()),
                new DefinedRenderer<>(current),
                definition.alterablePartsOrAll());
    }

}
