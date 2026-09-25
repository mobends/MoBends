package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityBender<T extends EntityLivingBase>
{
	protected final String key;
	protected final String unlocalizedName;

	private final MutatedRenderer<T> renderer;
	public final Class<T> entityClass;

	/**
	 * Per renderer: this bender's mutator and the renderer's state with the mutation in place, or
	 * {@link #NOT_MUTABLE} if the renderer's model can't be mutated by this bender.
	 */
	private final Map<RenderLivingBase<?>, Mutation> mutations = new HashMap<>();
	private static final Mutation NOT_MUTABLE = new Mutation(null, null);

	private IEntityDataFactory<T> dataFactory;
	private boolean animate;
	protected Map<String, BoneMetadata> boneMetadataMap;

	private static class Mutation
	{
		final Mutator<?, ?, ?> mutator;
		final RendererState state;

		Mutation(Mutator<?, ?, ?> mutator, RendererState state)
		{
			this.mutator = mutator;
			this.state = state;
		}
	}

	public EntityBender(String modId, @Nullable String key, String unlocalizedName, Class<T> entityClass,
						MutatedRenderer<T> renderer)
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

		this.key = modId + "-" + key;
		this.unlocalizedName = unlocalizedName;
		this.entityClass = entityClass;
		this.renderer = renderer;
	}

	public abstract String[] getAlterableParts();

	public abstract IEntityDataFactory<T> getDataFactory();

	public abstract IMutatorFactory<T> getMutatorFactory();

	/**
	 * The data factory entities of this bender get when their type keeps the bender's own animator.
	 * Always the same instance, which is how the entity database tells whose data it holds.
	 */
	public IEntityDataFactory<T> getDefaultDataFactory()
	{
		if (this.dataFactory == null)
			this.dataFactory = this.getDataFactory();
		return this.dataFactory;
	}

	public String getKey()
	{
		return this.key;
	}

	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}

	public String getLocalizedName()
	{
		return I18n.format(this.unlocalizedName);
	}

	/**
	 * Returns true if entities assigned to this EntityBender
	 * should be animated.
	 */
	public boolean isAnimated()
	{
		return this.animate;
	}

	public void setAnimate(boolean animate)
	{
		this.animate = animate;
	}

	public void beforeRender(EntityData<T> data, T entity, float partialTicks)
	{
		this.renderer.beforeRender(data, entity, partialTicks);
	}

	public void afterRender(T entity, float partialTicks)
	{
		this.renderer.afterRender(entity, partialTicks);
	}

	/**
	 * Puts this bender's mutation in place on the renderer (mutating it the first time) and animates
	 * the entity with data made by {@code dataFactory}. The caller restores the renderer to vanilla
	 * after the render, see {@link RendererState}.
	 *
	 * @return False if the renderer's model can't be mutated by this bender.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean applyMutation(RenderLivingBase<? extends T> renderer, T entity, IEntityDataFactory<T> dataFactory, float partialTicks)
	{
		Mutator mutator = this.attachMutation(renderer);
		if (mutator == null)
		{
			return false;
		}

		mutator.updateModel(entity, renderer, partialTicks);
		LivingEntityData<T> data = EntityDatabase.instance.getOrMake(dataFactory, entity);
		mutator.performAnimations(data, this.key, renderer, partialTicks);
		mutator.syncUpWithData(data);

		return true;
	}

	/**
	 * Puts this bender's mutation in place on the renderer, mutating it first if it never was.
	 *
	 * @return The mutator, or null if the renderer's model can't be mutated by this bender.
	 */
	@Nullable
	public Mutator<?, ?, ?> attachMutation(RenderLivingBase<? extends T> renderer)
	{
		Mutation mutation = mutations.get(renderer);
		if (mutation == null)
		{
			// Mutating starts from the vanilla renderer, whatever another bender left in place.
			RendererState.vanillaOf(renderer);
			RendererState.restoreVanilla(renderer);

			Mutator<? extends LivingEntityData<T>, ? extends T, ?> mutator = this.getMutatorFactory().createMutator();
			if (mutateWith(mutator, renderer))
			{
				mutation = new Mutation(mutator, RendererState.capture(renderer));
				RendererState.markMutated(renderer);
			}
			else
			{
				RendererState.restoreVanilla(renderer);
				mutation = NOT_MUTABLE;
			}
			mutations.put(renderer, mutation);
		}
		else if (mutation != NOT_MUTABLE)
		{
			mutation.state.applyMutation(renderer);
		}
		return mutation.mutator;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean mutateWith(Mutator mutator, RenderLivingBase<?> renderer)
	{
		return mutator.mutate(renderer);
	}

	/**
	 * Drops every mutation (the renderers go back to vanilla), so they are rebuilt from (reloaded)
	 * resources on the next render.
	 */
	public void refreshMutation()
	{
		for (RenderLivingBase<?> renderer : mutations.keySet())
		{
			RendererState.restoreVanilla(renderer);
		}
		mutations.clear();
	}

	@Nullable
	public Mutator<?, ?, ?> getMutator(RenderLivingBase<? extends EntityLivingBase> renderer)
	{
		Mutation mutation = this.mutations.get(renderer);
		return mutation == null ? null : mutation.mutator;
	}
}
