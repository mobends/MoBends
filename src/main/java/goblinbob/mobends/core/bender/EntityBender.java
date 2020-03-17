package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.math.TransformUtils;
import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class EntityBender<T extends EntityLivingBase>
{

	protected final String key;
	protected final String unlocalizedName;

	private final MutatedRenderer<T> renderer;
	public final Class<T> entityClass;

	private final Map<RenderLivingBase<? extends T>, Mutator<LivingEntityData<T>, T, ?>> mutatorMap = new HashMap<>();

	private boolean animate;
	protected Map<String, BoneMetadata> boneMetadataMap;

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

	public abstract IPreviewer<?> getPreviewer();

	public abstract LivingEntityData<?> getDataForPreview();

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
		ArmorModelFactory.updateMutation();
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
	 * Used to apply the effect of the mutation, or just to update the model if it was already mutated.
	 * Called from EntityBender.
	 */
	@SuppressWarnings("unchecked")
	public boolean applyMutation(RenderLivingBase<? extends T> renderer, T entity, float partialTicks)
	{
		Mutator<LivingEntityData<T>, T, ?> mutator = mutatorMap.get(renderer);
		if (mutator == null)
		{
			mutator = (Mutator<LivingEntityData<T>, T, ?>) this.getMutatorFactory().createMutator(this.getDataFactory());
			if (!mutator.mutate(renderer))
			{
				return false;
			}

			mutatorMap.put(renderer, mutator);
		}

		mutator.updateModel(entity, renderer, partialTicks);
		LivingEntityData<T> data = mutator.getOrMakeData(entity);
		mutator.performAnimations(data, this.key, renderer, partialTicks);
		mutator.syncUpWithData(data);

		return true;
	}

	/**
	 * Used to reverse the effect of the mutation.
	 * Called from EntityBender.
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

	/**
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

	@SuppressWarnings("unchecked")
	protected T createPreviewEntity()
	{
		try
		{
			EntityLiving entity = (EntityLiving) this.entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().world);
			entity.world = Minecraft.getMinecraft().world;
			entity.setLocationAndAngles(0, 0, 0, 0, 0);
			entity.onInitialSpawn(entity.world.getDifficultyForLocation(entity.getPosition()), null);
			PreviewHelper.registerPreviewEntity(entity);

			return (T) entity;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public void transformModelToCharacterSpace(IMat4x4d matrixOut)
	{
		TransformUtils.scale(matrixOut, -1.0F, -1.0F, 1.0F);
		TransformUtils.translate(matrixOut, 0.0F, -1.501F, 0.0F);
	}

	public Mutator<?, ?, ?> getMutator(RenderLivingBase<? extends EntityLivingBase> renderer)
	{
		return this.mutatorMap.get(renderer);
	}

	// -- Static Methods --
	
	public static <T extends EntityLivingBase> Mutator<?, ?, ?> getMutatorForRenderer(Class<T> entityClass, RenderLivingBase<T> renderer)
	{
		final EntityBender<?> bender = EntityBenderRegistry.instance.getForEntityClass(entityClass);
		return bender != null ? bender.mutatorMap.get(renderer) : null;
	}
	
}
