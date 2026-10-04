package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.driver.IDriverFactory;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.core.types.selector.ISelectorConditionFactory;
import goblinbob.mobends.core.types.selector.SelectorConditionRegistry;
import net.minecraft.entity.EntityLivingBase;

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
     * Registers an operation animators can use in their expressions, as "modid:key".
     * @param key The internal name of the operation. (snake_case preferable)
     * @param params What its arguments are (see {@link ExpressionOperations#number} and the others).
     * @param repeatsLast Whether its last parameter repeats.
     * @param factory Makes the operation from its compiled arguments.
     */
    public void registerOperation(String key, ExpressionOperations.Param[] params, boolean repeatsLast, ExpressionOperations.Factory factory)
    {
        ExpressionOperations.register(String.format("%s:%s", modId, key), params, repeatsLast, factory);
    }

    /**
     * Registers a pose driver usable from animator JSON as "modid:key".
     */
    public <T extends DriverItemTemplate> void registerDriver(String key, IDriverFactory<T> factory, Class<T> templateType)
    {
        DriverRegistry.INSTANCE.register(String.format("%s:%s", modId, key), factory, templateType);
    }

    /**
     * Registers a condition that type files can use in their selectors (see misc/kumo-format.md).
     * @param key The internal name of the condition. (snake_case preferable)
     *            This is going to be automatically prefixed with the modid like so "modid:key"
     * @param factory Makes the condition from its JSON object.
     */
    public void registerSelectorCondition(String key, ISelectorConditionFactory factory)
    {
        SelectorConditionRegistry.INSTANCE.register(String.format("%s:%s", modId, key), factory);
    }

}
