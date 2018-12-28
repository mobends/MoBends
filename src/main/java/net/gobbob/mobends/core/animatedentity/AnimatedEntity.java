package net.gobbob.mobends.core.animatedentity;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.LivingEntityData;
import net.gobbob.mobends.core.client.MutatedRenderer;
import net.gobbob.mobends.core.mutators.IMutatorFactory;
import net.gobbob.mobends.core.mutators.MutationContainer;
import net.gobbob.mobends.core.mutators.Mutator;
import net.gobbob.mobends.core.util.Lang;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class AnimatedEntity<T extends EntityLivingBase>
{
	String modid;
	String key;
	String keyWithoutModId;
	String unlocalizedName;
	List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	private String[] alterableParts;
	private MutatedRenderer<T> renderer;
	public Class<T> entityClass;

	public MutationContainer mutationContainer;
	public Previewer previewer;

	public AnimatedEntity(String key, String unlocalizedName, Class<T> entityClass,
			IMutatorFactory mutatorFactory,
			MutatedRenderer renderer, String[] alterableParts)
	{
		this.keyWithoutModId = key;
		if (this.keyWithoutModId != null)
			this.key = modid + "-" + this.keyWithoutModId;
		this.unlocalizedName = unlocalizedName;
		this.entityClass = entityClass;
		this.mutationContainer = new MutationContainer(mutatorFactory);
		this.renderer = renderer;
		this.alterableParts = alterableParts;	
	}
	
	public AnimatedEntity(Class<T> entityClass,
			IMutatorFactory mutatorFactory,
			MutatedRenderer renderer, String[] alterableParts)
	{
		this(null, null, entityClass, mutatorFactory, renderer, alterableParts);
	}
	
	public boolean onRegistraton()
	{
		if (this.key == null)
		{
			ResourceLocation key = EntityList.getKey(entityClass);
			if (key == null)
				return false;
			
			this.key = modid + "-" + key;
			this.unlocalizedName = "entity." + EntityList.getTranslationName(key) + ".name";
		}
		
		// Creating the default alter entry if none has been added
		generateDefaultAlterEntry();
		
		for (AlterEntry entry : this.alterEntries)
		{
			entry.onRegistered(this);
		}
		
		return true;
	}
	
	public void generateDefaultAlterEntry()
	{
		if (this.alterEntries.size() == 0)
		{
			this.alterEntries.add(new AlterEntry());
		}
	}
	
	public List<AlterEntry> getAlterEntries()
	{
		return this.alterEntries;
	}

	public AlterEntry getAlterEntry(int index)
	{
		return this.alterEntries.get(index);
	}

	public String[] getAlterableParts()
	{
		return alterableParts;
	}

	public String getLocalizedName()
	{
		return Lang.localize(this.unlocalizedName);
	}

	public String getKey()
	{
		return key;
	}

	public Previewer<T> getPreviewer()
	{
		return this.previewer;
	}

	/*
	 * Returns true if any of the alter entries are animated
	 */
	public boolean isAnimated()
	{
		for (AlterEntry entry : this.alterEntries)
		{
			if (entry.isAnimated())
				return true;
		}
		return false;
	}
	
	public AnimatedEntity setModId(String modid)
	{
		this.modid = modid;
		if (this.keyWithoutModId != null)
			this.key = modid + "-" + this.keyWithoutModId;
		return this;
	}
	
	public AnimatedEntity setAlterEntry(AlterEntry alterEntry)
	{
		this.alterEntries.clear();
		this.alterEntries.add(alterEntry);
		return this;
	}

	public AnimatedEntity<T> addAlterEntry(AlterEntry alterEntry)
	{
		this.generateDefaultAlterEntry();
		
		this.alterEntries.add(alterEntry);
		return this;
	}

	public AnimatedEntity<T> setPreviewer(Previewer<T> previewer)
	{
		this.previewer = previewer;
		return this;
	}

	public void beforeRender(T entity, float partialTicks)
	{
		if (this.renderer != null)
			this.renderer.beforeRender(entity, partialTicks);
	}

	public void afterRender(T entity, float partialTicks)
	{
		if (this.renderer != null)
			this.renderer.afterRender(entity, partialTicks);
	}

	public boolean applyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity,
			float partialTicks)
	{
		return mutationContainer.apply(renderer, entity, partialTicks);
	}

	public void deapplyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity)
	{
		mutationContainer.deapply(renderer, entity);
	}

	public void refreshMutation()
	{
		mutationContainer.refresh();
	}

	public static <E extends EntityLivingBase> AnimatedEntity<E> getForEntity(E entity)
	{
		return (AnimatedEntity<E>) AnimatedEntityRegistry.getForEntity(entity);
	}
	
	public static void refreshMutators()
	{
		AnimatedEntityRegistry.refreshMutators();
	}
	
	public static <D extends LivingEntityData<E>, E extends EntityLivingBase, M extends ModelBase> Mutator<D, E, M> getMutatorForRenderer(Class<? extends Entity> entityClass, RenderLivingBase<? extends E> renderer)
	{
		AnimatedEntity ae = AnimatedEntityRegistry.getForEntityClass(entityClass);
		if (ae == null)
			return null;
		
		return ae.mutationContainer.getForRenderer(renderer);
	}
}
