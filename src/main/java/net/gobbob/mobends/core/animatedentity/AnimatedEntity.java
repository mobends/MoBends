package net.gobbob.mobends.core.animatedentity;

import java.util.ArrayList;
import java.util.List;

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
	String unlocalizedName;
	List<AlterEntry> alterEntries = new ArrayList<AlterEntry>();
	private String[] alterableParts;
	private MutatedRenderer<T> renderer;
	public Class<? extends Entity> entityClass;

	public MutationContainer mutationContainer;
	public Previewer previewer;

	public AnimatedEntity(String modid, String key, String unlocalizedName, Class<? extends Entity> entityClass,
			IMutatorFactory mutatorFactory,
			MutatedRenderer renderer, String[] alterableParts)
	{
		init(modid, key, unlocalizedName, entityClass, mutatorFactory, renderer, alterableParts);
	}
	
	public AnimatedEntity(String modid, Class<? extends Entity> entityClass,
			IMutatorFactory mutatorFactory,
			MutatedRenderer renderer, String[] alterableParts)
	{
		ResourceLocation key = EntityList.getKey(entityClass);
		String name = "entity." + EntityList.getTranslationName(key) + ".name";
		String keyString = modid + "-" + key.toString();
		
		init(modid, keyString, name, entityClass,
			 mutatorFactory, renderer, alterableParts);
	}
	
	private void init(String modid, String key, String unlocalizedName, Class<? extends Entity> entityClass,
			IMutatorFactory mutatorFactory,
			MutatedRenderer renderer, String[] alterableParts)
	{
		this.modid = modid;
		this.key = key;
		this.unlocalizedName = unlocalizedName;
		this.entityClass = entityClass;
		this.mutationContainer = new MutationContainer(mutatorFactory);
		this.renderer = renderer;
		this.alterableParts = alterableParts;
		this.addAlterEntry(new AlterEntry(this, key, unlocalizedName));
	}
	
	public List<AlterEntry> getAlredEntries()
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

	public Previewer getPreviewer()
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

	public AnimatedEntity addAlterEntry(AlterEntry alterEntry)
	{
		this.alterEntries.add(alterEntry);
		return this;
	}

	public AnimatedEntity setPreviewer(Previewer previewer)
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

	public void applyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity,
			float partialTicks)
	{
		mutationContainer.apply(renderer, entity, partialTicks);
	}

	public void deapplyMutation(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity)
	{
		mutationContainer.deapply(renderer, entity);
	}

	public void refreshMutation()
	{
		mutationContainer.refresh();
	}

	public static AnimatedEntity getForEntity(Entity entity)
	{
		return AnimatedEntityRegistry.getForEntity(entity);
	}
	
	public static void refreshMutators()
	{
		AnimatedEntityRegistry.refreshMutators();
	}
	
	public static <E extends EntityLivingBase, M extends ModelBase> Mutator<E, M> getMutatorForRenderer(Class<? extends Entity> entityClass, RenderLivingBase<? extends E> renderer)
	{
		AnimatedEntity ae = AnimatedEntityRegistry.getForEntityClass(entityClass);
		if (ae == null)
			return null;
		
		return ae.mutationContainer.getForRenderer(renderer);
	}
}
