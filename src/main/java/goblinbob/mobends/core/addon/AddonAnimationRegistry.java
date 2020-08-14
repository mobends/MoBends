package goblinbob.mobends.core.addon;

import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.bender.IPreviewer;
import goblinbob.mobends.core.client.gui.AnimationEditorRegistry;
import goblinbob.mobends.core.client.gui.IAnimationEditor;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionFactory;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.entity.EntityLivingBase;

public class AddonAnimationRegistry
{

    private final String modId;

    public AddonAnimationRegistry(String modId)
    {
        this.modId = modId;
    }

    /**
     * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer,
     * String...)}, but the key and unlocalizedName are decided based on how the entity was registered.
     */
    public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer, String... alterableParts)
    {
        return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, alterableParts);
    }

    /**
     * Works like {@link #registerNewEntity(Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, IPreviewer,
     * String...)}, but the key and unlocalizedName are decided based on how the entity was registered.
     */
    public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer, IPreviewer<?> previewer, String... alterableParts)
    {
        return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
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
     * @param alterableParts    A list of the entity's parts that can be animated.
     *
     * @return The entity's identifier key.
     */
    public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer, String... alterableParts)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, null, alterableParts);
        return registerEntity(entityBender);
    }

    /**
     * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer,
     * String...)}, but you can specify a custom previewer.
     */
    public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
                                                                 IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                                                                 MutatedRenderer<T> renderer, IPreviewer<?> previewer, String... alterableParts)
    {
        EntityBender<T> entityBender = new DefaultEntityBender<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
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
        if (!key.startsWith(this.modId))
        {
            throw new IllegalArgumentException("The EntityBender's ModID does not match that of the AddonAnimationRegistry.");
        }
        EntityBenderRegistry.instance.registerBender(entityBender);
        return entityBender.getKey();
    }

    /**
     * Registers a trigger condition that can be used in KUMO internal animators as well as BendsPacks.
     * @param key The internal name of the trigger condition. (snake_case preferable)
     *            This is going to be automatically prefixed with the modid like so "modid:key"
     * @param factory The constructor of the trigger condition instance.
     * @param templateType The type of the template that this condition is going to be serialized into.
     */
    public <T extends TriggerConditionTemplate> void registerTriggerCondition(String key, ITriggerConditionFactory<?, T> factory, Class<T> templateType)
    {
        TriggerConditionRegistry.instance.register(String.format("%s:%s", modId, key), factory, templateType);
    }

    /**
     * Registers a trigger condition that can be used in KUMO internal animators as well as BendsPacks.
     * @param key The internal name of the trigger condition. (snake_case preferable)
     *            This is going to be automatically prefixed with the modid like so "modid:key"
     * @param condition The trigger condition instance.
     */
    public void registerTriggerCondition(String key, ITriggerCondition condition)
    {
        TriggerConditionRegistry.instance.register(String.format("%s:%s", modId, key), condition);
    }

    public void registerAnimationEditor(IAnimationEditor editor)
    {
        AnimationEditorRegistry.INSTANCE.registerEditor(editor);
    }

}
