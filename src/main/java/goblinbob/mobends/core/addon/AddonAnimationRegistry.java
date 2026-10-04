package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberFunctions;
import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.client.definition.DefinedBenders;
import goblinbob.mobends.core.client.definition.DefinedLayers;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.driver.IDriverFactory;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.function.ToDoubleFunction;

public class AddonAnimationRegistry
{

    private final String modId;

    public AddonAnimationRegistry(String modId)
    {
        this.modId = modId;
    }

    /**
     * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer)},
     * but the key and unlocalizedName are decided based on how the entity was registered.
     */
    public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer)
    {
        return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer);
    }

    /**
     * Registers the entity as an animated one. The system will then mutate all entities belonging to the specified
     * EntityClass, and apply custom animations.
     *
     * @param key               A custom key for this entity.
     * @param unlocalizedName   The language-key to use for this entity's name.
     * @param entityClass       The entity class that should be animated.
     * @param entityDataFactory Responsible for creating an entity's data.
     * @param mutatorFactory    Responsible for creating an entity's mutator.
     * @param renderer          The renderer that will decide how this entity should be rendered.
     *
     * @return The entity's identifier key.
     */
    public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer);
        return registerEntity(entityBender);
    }

    /**
     * Registers a model definition of this addon ({@code <modid>:bends/models/<name>.json}) as the
     * default model of its entity class, as {@link #registerNewEntity} does a Java one: under the
     * key the definition names (else the entity's id), with the built-in type of that id. The
     * definition is read again when resources reload.
     *
     * @return The entity's identifier key, or null if the definition can't be read (logged).
     */
    @Nullable
    public String registerDefinedEntity(String name)
    {
        ResourceLocation location = ModelDefinitions.locationOf(modId, name);
        EntityBender<EntityLivingBase> entityBender;
        try
        {
            entityBender = DefinedBenders.createBender(modId, location, ModelDefinitions.INSTANCE.load(location));
        }
        catch (Exception e)
        {
            Core.LOG.log(Level.SEVERE, "Could not register the model definition " + location + ": " + e.getMessage());
            return null;
        }
        return registerEntity(entityBender);
    }

    /**
     * Use this in case you want to use a custom sub-type of EntityBender for extended functionality.
     *
     * @param entityBender An instance of the EntityBender to put into the system.
     *
     * @return The entity's identifier key.
     */
    public <T extends EntityLivingBase> String registerEntity(EntityBender<T> entityBender)
    {
        String key = entityBender.getKey();
        if (!key.startsWith(this.modId + ":"))
        {
            throw new IllegalArgumentException("The EntityBender's ModID does not match that of the AddonAnimationRegistry.");
        }
        EntityBenderRegistry.instance.registerBender(entityBender);
        return entityBender.getKey();
    }

    /**
     * Registers an operation animators can use in their expressions, as "modid:name" (the
     * operation is named without the mod id, in snake_case). See {@link KumoOperation} for the
     * signature, and {@link #registerFunction} and {@link #registerEntityFloatReader} for the
     * shorter ways.
     */
    public void registerOperation(KumoOperation operation)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerOperation(operation.renamed(namespaced(operation.name)));
    }

    /** Registers a pure function of numbers as "modid:key": computed once, when an animator loads, if its argument is written out. */
    public void registerFunction(String key, NumberFunctions.Unary function)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    public void registerFunction(String key, NumberFunctions.Binary function)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    public void registerFunction(String key, NumberFunctions.Ternary function)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerFunction(namespaced(key), function);
    }

    /**
     * Registers a number read from the entity, as "modid:key": {@code {"mymod:wetness": []}}.
     * {@code type} is where it applies: an entity of another class takes the operation's
     * {@code @fallback}, or its animator fails to load.
     */
    public <E extends Entity> void registerEntityFloatReader(String key, Class<E> type, ToDoubleFunction<? super E> reader)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityFloatReader(namespaced(key), type, reader);
    }

    /** Registers whether the entity is something, as "modid:key"; see {@link #registerEntityFloatReader}. */
    public <E extends Entity> void registerEntityBooleanReader(String key, Class<E> type, Predicate<? super E> reader)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityBooleanReader(namespaced(key), type, reader);
    }

    /**
     * Registers a pose driver usable from animator JSON as "modid:name" (the driver is named
     * without the mod id): see {@link KumoDriver}.
     */
    public <T extends DriverItemTemplate> void registerDriver(KumoDriver<T> driver)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerDriver(driver.renamed(namespaced(driver.name)));
    }

    /**
     * Registers a pose driver usable from animator JSON as "modid:key", the internal way: Mo'
     * Bends' own drivers move to {@link #registerDriver(KumoDriver)} with their declared state.
     */
    public <T extends DriverItemTemplate> void registerDriver(String key, IDriverFactory<T> factory, Class<T> templateType)
    {
        Addons.checkRegistrationOpen();
        DriverRegistry.INSTANCE.register(namespaced(key), factory, templateType);
    }

    /**
     * Registers a component model definitions can give an entity's data, as "modid:key" (their
     * {@code components}: {@code {"swordTrail": "mobends:sword_trail"}}).
     */
    public void registerComponent(String key, EntityComponents.Factory factory)
    {
        Addons.checkRegistrationOpen();
        EntityComponents.register(namespaced(key), factory);
    }

    /**
     * Registers a renderer layer model definitions can ask for, as "modid:key" (their
     * {@code layers}). It replaces the renderer's layers of class {@code replaces}, or, with null,
     * is added after them.
     */
    public void registerLayer(String key, @Nullable Class<?> replaces, DefinedLayers.Factory factory)
    {
        Addons.checkRegistrationOpen();
        DefinedLayers.register(namespaced(key), replaces, factory);
    }

    private String namespaced(String key)
    {
        if (key.indexOf(':') >= 0)
        {
            throw new IllegalArgumentException("'" + key + "' is registered as '" + modId + ":" + key + "': name it without a namespace.");
        }
        return modId + ":" + key;
    }

}
