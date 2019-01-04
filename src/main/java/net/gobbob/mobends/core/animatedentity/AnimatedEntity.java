package net.gobbob.mobends.core.animatedentity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.gobbob.mobends.core.client.MutatedRenderer;
import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.mutators.IMutatorFactory;
import net.gobbob.mobends.core.mutators.Mutator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class AnimatedEntity<T extends EntityLivingBase>
{
	private final String key;
	private final String unlocalizedName;
	private final List<AlterEntry<T>> alterEntries;
	private final String[] alterableParts;
	private final MutatedRenderer<T> renderer;
	public final Class<T> entityClass;

	private final HashMap<RenderLivingBase<? extends T>, Mutator<LivingEntityData<T>, T, ?>> mutatorMap = new HashMap<>();
	
	private final IEntityDataFactory<T> entityDataFactory;
	private final IMutatorFactory<T> mutatorFactory;
	public final Previewer<?> previewer;

	public AnimatedEntity(String modId, String key, String unlocalizedName, Class<T> entityClass,
			IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
			MutatedRenderer<T> renderer, Previewer<?> previwer, List<AlterEntry<T>> alterEntries, String... alterableParts)
	{
		if (renderer == null)
			throw new NullPointerException("The mutated renderer cannot be null.");
		if (entityClass == null)
			throw new NullPointerException("The entity class cannot be null.");
		if (modId == null)
			throw new NullPointerException("The Mod ID cannot be null.");
		
		
		if (key == null)
		{
			ResourceLocation resourceLocation = EntityList.getKey(entityClass);
			if (resourceLocation == null)
				throw new RuntimeException("Unable to find a key for " + entityClass.getName());
			key = resourceLocation.toString();
			unlocalizedName = "entity." + EntityList.getTranslationName(resourceLocation) + ".name";
		}
		
		if (alterEntries == null || alterEntries.isEmpty())
			this.alterEntries = Arrays.asList(new DefaultAlterEntry<T>());
		else
			this.alterEntries = Collections.unmodifiableList(alterEntries);
		
		this.key = modId + "-" + key;
		this.unlocalizedName = unlocalizedName;
		this.entityClass = entityClass;
		this.entityDataFactory = entityDataFactory;
		this.mutatorFactory = mutatorFactory;
		this.renderer = renderer;
		this.previewer = previwer;
		this.alterableParts = alterableParts;
	}
	
	public boolean onRegister()
	{
		for (AlterEntry<T> entry : this.alterEntries)
		{
			entry.onRegister(this);
		}
		return true;
	}
	
	public List<AlterEntry<T>> getAlterEntries()
	{
		return this.alterEntries;
	}

	public String[] getAlterableParts()
	{
		return alterableParts;
	}

	public String getLocalizedName()
	{
		return I18n.format(this.unlocalizedName);
	}

	public String getKey()
	{
		return this.key;
	}
	
	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}
	
	public IEntityDataFactory<T> getDataFactory()
	{
		return this.entityDataFactory;
	}

	/*
	 * Returns true if any of the alter entries are animated
	 */
	public boolean isAnimated()
	{
		for (AlterEntry<T> entry : this.alterEntries)
		{
			if (entry.isAnimated())
				return true;
		}
		return false;
	}

	public void beforeRender(T entity, float partialTicks)
	{
		this.renderer.beforeRender(entity, partialTicks);
	}

	public void afterRender(T entity, float partialTicks)
	{
		this.renderer.afterRender(entity, partialTicks);
	}

	/*
	 * Used to apply the effect of the mutation, or just to update the model if it was already mutated.
	 * Called from AnimatedEntity.
	 */
	@SuppressWarnings("unchecked")
	public boolean applyMutation(RenderLivingBase<? extends T> renderer, T entity, float partialTicks)
	{
		Mutator<LivingEntityData<T>, T, ?> mutator = mutatorMap.get(renderer);
		if (mutator == null)
		{
			mutator = (Mutator<LivingEntityData<T>, T, ?>) mutatorFactory.createMutator(entityDataFactory);
			if (!mutator.mutate(renderer))
			{
				return false;
			}
			
			mutatorMap.put(renderer, mutator);
		}

		mutator.updateModel(entity, renderer, partialTicks);
		LivingEntityData<T> data = mutator.getOrMakeData(entity);
		mutator.performAnimations(data, renderer, partialTicks);
		mutator.syncUpWithData(data);
		
		return true;
	}

	/*
	 * Used to reverse the effect of the mutation.
	 * Called from AnimatedEntity.
	 */
	public void deapplyMutation(RenderLivingBase<? extends T> renderer, EntityLivingBase entity)
	{
		if (mutatorMap.containsKey(renderer))
		{
			Mutator<LivingEntityData<T>, T, ?> mutator = mutatorMap.get(renderer);
			mutator.demutate(renderer);
			mutatorMap.remove(renderer);
		}
	}

	/*
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public void refreshMutation()
	{
		for (Entry<RenderLivingBase<? extends T>, Mutator<LivingEntityData<T>, T, ?>> entry : mutatorMap.entrySet())
		{
			Mutator<?, T, ?> mutator = entry.getValue();
			mutator.mutate(entry.getKey());
			mutator.postRefresh();
		}
	}
	
	public static <T extends EntityLivingBase> Mutator<?, ?, ?> getMutatorForRenderer(Class<T> entityClass, RenderLivingBase<T> renderer)
	{
		AnimatedEntity<?> ae = AnimatedEntityRegistry.getForEntityClass(entityClass);
		if (ae == null)
			return null;
		return ae.mutatorMap.get(renderer);
	}
}
