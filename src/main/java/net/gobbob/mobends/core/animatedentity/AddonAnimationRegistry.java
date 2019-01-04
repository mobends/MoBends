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
	
	
	public <T extends EntityLivingBase> String registerNewEntity(Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, Previewer<?> previewer, List<AlterEntry<T>> alterEntries,  String... alterableParts)
	{
		return registerNewEntity(null, null, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterEntries, alterableParts);
	}
	
	
	public <T extends EntityLivingBase> String registerNewEntity(String key, String unlocalizedName, Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, Previewer<?> previewer, List<AlterEntry<T>> alterEntries, String... alterableParts)
	{
		AnimatedEntity<T> animatedEntity = new AnimatedEntity<T>(modId, key, unlocalizedName, entityClass, entityDataFactory, mutatorFactory, renderer, previewer, alterEntries, alterableParts);
		return registerEntity(animatedEntity);
	}
	
	public <T extends EntityLivingBase> String registerEntity(AnimatedEntity<T> animatedEntity) {
		String key = animatedEntity.getKey();
		if (!key.startsWith(this.modId))
		{
			throw new IllegalArgumentException("The AnimatedEntity's ModID does not match that of the AddonAnimationRegistry.");
		}
		AnimatedEntityRegistry.INSTANCE.registerEntity(animatedEntity);
		return animatedEntity.getKey();
	}
	
}
