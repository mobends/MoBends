package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.resources.I18n;
import goblinbob.mobends.core.compat.EntityIds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class EntityBender<T extends EntityLivingBase>
{
	/**
	 * Identifies the bender (its model), a resource location: the registering mod and the name the
	 * mod gave it, or else the entity's id ({@code mobends:zombie}; {@code mobends:othermod/beast}
	 * for an entity of another mod than Minecraft). Also the id of the bender's built-in entity type.
	 */
	protected final String key;
	/** The key before 2.0 ({@code mobends:zombie}), for settings saved by older versions. */
	private final String legacyKey;
	protected final String unlocalizedName;

	private final MutatedRenderer<T> renderer;
	public final Class<T> entityClass;

	/**
	 * Per renderer: this bender's mutator and the renderer's state with the mutation in place, or
	 * {@link #NOT_MUTABLE} if the renderer's model can't be mutated by this bender.
	 */
	/** By renderer, then by the renderer's main model (a renderer may swap models, see {@link RendererState}). */
	private final Map<RendererLivingEntity<?>, Map<ModelBase, Mutation>> mutations = new HashMap<>();
	private static final Mutation NOT_MUTABLE = new Mutation(null, null);

	private IEntityDataFactory<T> dataFactory;
	private boolean animate;
	/** The resource pack whose type brought this bender, or null if it isn't a resource pack's. */
	@Nullable
	private String resourcePack;
	private Supplier<OptionSet> options = () -> OptionSet.EMPTY;

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

		String name = key;
		if (key == null)
		{
			ResourceLocation resourceLocation = EntityIds.ofClass(entityClass);
			if (resourceLocation == null)
				throw new RuntimeException("Unable to find a key for " + entityClass.getName());
			key = resourceLocation.toString();
			name = "minecraft".equals(resourceLocation.getResourceDomain())
					? resourceLocation.getResourcePath()
					: resourceLocation.getResourceDomain() + "/" + resourceLocation.getResourcePath();
			unlocalizedName = EntityIds.translationKey(entityClass);
		}

		this.key = new ResourceLocation(modId, name).toString();
		this.legacyKey = modId + "-" + key;
		this.unlocalizedName = unlocalizedName;
		this.entityClass = entityClass;
		this.renderer = renderer;
	}

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

	/** The key versions before 2.0 used (see {@link #key}), under which they saved settings. */
	public String getLegacyKey()
	{
		return this.legacyKey;
	}

	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}

	public String getLocalizedName()
	{
		return I18n.format(this.unlocalizedName);
	}

	/** The resource pack whose type brought this bender (a mob it animates), or null if it isn't a resource pack's. */
	@Nullable
	public String getResourcePack()
	{
		return this.resourcePack;
	}

	public void setResourcePack(@Nullable String resourcePack)
	{
		this.resourcePack = resourcePack;
	}

	/** The options its model definition declares, as loaded now; none if it has no definition. */
	public OptionSet getOptions()
	{
		return this.options.get();
	}

	public void setOptions(Supplier<OptionSet> options)
	{
		this.options = options;
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

	/**
	 * Puts this bender's mutation in place on the renderer (mutating it the first time) and animates
	 * the entity with data made by {@code dataFactory}. The caller restores the renderer to vanilla
	 * after the render, see {@link RendererState}.
	 *
	 * @return False if the renderer's model can't be mutated by this bender, or if the entity's
	 *         animator asks for vanilla right now (the renderer is then put back to vanilla, after
	 *         animating, so the animator's graph keeps running).
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean applyMutation(RendererLivingEntity<? extends T> renderer, T entity, IEntityDataFactory<T> dataFactory, float partialTicks)
	{
		Mutator mutator = this.attachMutation(renderer);
		if (mutator == null)
		{
			return false;
		}

		mutator.updateModel(entity, renderer, partialTicks);
		LivingEntityData<T> data = EntityDatabase.instance.getOrMake(dataFactory, entity);
		mutator.performAnimations(data);
		mutator.syncUpWithData(data);

		if (data.wantsVanilla())
		{
			RendererState.restoreVanilla(renderer);
			return false;
		}
		return true;
	}

	/**
	 * Puts this bender's mutation in place on the renderer, mutating it first if it never was.
	 *
	 * @return The mutator, or null if the renderer's model can't be mutated by this bender.
	 */
	@Nullable
	public Mutator<?, ?, ?> attachMutation(RendererLivingEntity<? extends T> renderer)
	{
		final Map<ModelBase, Mutation> byModel = mutations.computeIfAbsent(renderer, r -> new IdentityHashMap<>());
		// The vanilla model the renderer draws this entity with (it swaps back to it before every draw).
		final ModelBase model = renderer.getMainModel();
		Mutation mutation = byModel.get(model);
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
			byModel.put(model, mutation);
		}
		else if (mutation != NOT_MUTABLE)
		{
			mutation.state.applyMutation(renderer);
		}
		return mutation.mutator;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean mutateWith(Mutator mutator, RendererLivingEntity<?> renderer)
	{
		return mutator.mutate(renderer);
	}

	/** Sets up an entity made for the Settings preview, before its renderer is selected. */
	public void preparePreview(EntityLivingBase entity)
	{
	}

	/**
	 * Drops every mutation (the renderers go back to vanilla), so they are rebuilt from (reloaded)
	 * resources on the next render.
	 */
	public void refreshMutation()
	{
		for (RendererLivingEntity<?> renderer : mutations.keySet())
		{
			// Only the model the renderer holds now: the others are restored as they come back (see RendererState).
			RendererState.restoreVanilla(renderer);
		}
		mutations.clear();
	}

	@Nullable
	public Mutator<?, ?, ?> getMutator(RendererLivingEntity<? extends EntityLivingBase> renderer)
	{
		Map<ModelBase, Mutation> byModel = this.mutations.get(renderer);
		Mutation mutation = byModel == null ? null : byModel.get(renderer.getMainModel());
		return mutation == null ? null : mutation.mutator;
	}
}
