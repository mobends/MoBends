package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberFunctions;
import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.driver.IDriverFactory;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Predicate;
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
     * signature, and {@link #registerFunction} and {@link #registerEntityNumber} for the
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
    public <E extends Entity> void registerEntityNumber(String key, Class<E> type, ToDoubleFunction<? super E> reader)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityNumber(namespaced(key), type, reader);
    }

    /** Registers whether the entity is something, as "modid:key"; see {@link #registerEntityNumber}. */
    public <E extends Entity> void registerEntityCondition(String key, Class<E> type, Predicate<? super E> reader)
    {
        Addons.checkRegistrationOpen();
        KumoRegistry.registerEntityCondition(namespaced(key), type, reader);
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

    private String namespaced(String key)
    {
        if (key.indexOf(':') >= 0)
        {
            throw new IllegalArgumentException("'" + key + "' is registered as '" + modId + ":" + key + "': name it without a namespace.");
        }
        return modId + ":" + key;
    }

}
