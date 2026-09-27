package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;
import java.util.logging.Level;

/** Makes the animated entity (bender) of a model definition, for the type that names it. */
public final class DefinedBenders
{

    private DefinedBenders()
    {
    }

    /**
     * The bender of the model definition at {@code location} ({@code definition} is it as loaded
     * now), not registered yet. Its key is prefixed with {@code modId}. The entity's data, model and
     * renderer read the definition again whenever they are made (after a reload, or when the server
     * changes what resource packs may do), so the geometry always comes from where it's allowed to.
     */
    public static <E extends EntityLivingBase> EntityBender<E> createBender(String modId, ResourceLocation location, EntityModelDefinition definition) throws MalformedKumoTemplateException
    {
        Class<E> entityClass = entityClass(definition.entity);
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
                new DefinedRenderer<>(current));
    }

    /** The entity class a definition names; looked up without initialising it, as a resource pack may name any class. */
    @SuppressWarnings("unchecked")
    private static <E extends EntityLivingBase> Class<E> entityClass(String name) throws MalformedKumoTemplateException
    {
        Class<?> type;
        try
        {
            type = Class.forName(name, false, DefinedBenders.class.getClassLoader());
        }
        catch (ClassNotFoundException | LinkageError e)
        {
            throw new MalformedKumoTemplateException("There is no entity class " + name + ".");
        }
        if (!EntityLivingBase.class.isAssignableFrom(type))
        {
            throw new MalformedKumoTemplateException(name + " is not a living entity.");
        }
        return (Class<E>) type;
    }

}
