package net.gobbob.mobends.core.animatedentity;

import java.util.List;

import net.gobbob.mobends.core.client.MutatedRenderer;
import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.entity.EntityLivingBase;

public class AddonAnimationRegistry
{
	
	private final String modId;
	
	public AddonAnimationRegistry(String modId)
	{
		this.modId = modId;
	}
	
	/**
	 * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, String...)},
	 * but the key and unlocalizedName are decided based on how the entity was registered.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, String... alterableParts)
	{
		return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, alterableParts);
	}
	
	/**
	 * Works like {@link #registerNewEntity(Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, Previewer, String...)},
	 * but the key and unlocalizedName are decided based on how the entity was registered.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, Previewer<?> previewer, String... alterableParts)
	{
		return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
	}
	
	/**
	 * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, List, String...)},
	 * but the key and unlocalizedName are decided based on how the entity was registered.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, List<AlterEntry<T>> alterEntries,  String... alterableParts)
	{
		return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, alterEntries, alterableParts);
	}
	
	/**
	 * Registers the entity as an animated one. The system will then mutate all entities
	 * belonging to the specified EntityClass, and apply custom animations.
	 * 
	 * @param key				A custom key for this entity.
	 * @param unlocalizedName   The language-key to use for this entity's name.
	 * @param entityClass 		The entity class that should be animated.
	 * @param entityDataFactory Responsible for creating an entity's data.
	 * @param mutatorFactory	Responsible for creating an entity's mutator.
	 * @param renderer 			The renderer that will decide how this entity should be rendered.
	 * @param alterableParts 	A list of the entity's parts that can be animated.
	 * @return 					The entity's identifier key.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, String... alterableParts)
	{
		AnimatedEntity<T> animatedEntity = new AnimatedEntity<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, alterableParts);
		return registerEntity(animatedEntity);
	}
	
	/**
	 * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, String...)},
	 * but you can specify a custom previewer.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, Previewer<?> previewer, String... alterableParts)
	{
		AnimatedEntity<T> animatedEntity = new AnimatedEntity<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterableParts);
		return registerEntity(animatedEntity);
	}
	
	/**
	 * Works like {@link #registerNewEntity(String, String, Class, IEntityDataFactory, IMutatorFactory, MutatedRenderer, String...)},
	 * but you can specify a custom list of alter entries.
	 */
	public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, List<AlterEntry<T>> alterEntries, String... alterableParts)
	{
		AnimatedEntity<T> animatedEntity = new AnimatedEntity<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, alterEntries, alterableParts);
		return registerEntity(animatedEntity);
	}
	
	/**
	 * Use this in case you want to use a custom sub-type of AnimatedEntity for extended functionality.
	 * 
	 * @param animatedEntity An instance of the AnimatedEntity to put into the system.
	 * @return 				 The entity's identifier key.
	 */
	public <T extends EntityLivingBase> String registerEntity(AnimatedEntity<T> animatedEntity)
	{
		String key = animatedEntity.getKey();
		if (!key.startsWith(this.modId))
		{
			throw new IllegalArgumentException("The AnimatedEntity's ModID does not match that of the AddonAnimationRegistry.");
		}
		AnimatedEntityRegistry.INSTANCE.registerEntity(animatedEntity);
		return animatedEntity.getKey();
	}
	
}
