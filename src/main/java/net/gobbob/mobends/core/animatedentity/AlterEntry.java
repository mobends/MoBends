package net.gobbob.mobends.core.animatedentity;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public abstract class AlterEntry<T extends EntityLivingBase>
{
	/**
	 * The set of registered preview entities. This is used to determine if the system
	 * should refrain from removing an entity's data, since they aren't a part of the world
	 * and the system will think of them as dead.
	 */
	private static final Set<Entity> previewEntities = new HashSet<>();
	
	public static void registerPreviewEntity(Entity entity)
	{
		previewEntities.add(entity);
	}
	
	public static boolean isPreviewEntity(Entity entity)
	{
		return previewEntities.contains(entity);
	}
	
	String key;
	String unlocalizedName;
	String postfix;
	protected Previewer<?> previewer;
	private boolean animate;
	protected AnimatedEntity<T> owner;
	
	public AlterEntry(String postfix, String unlocalizedName)
	{
		this.postfix = postfix;
		this.unlocalizedName = unlocalizedName;
	}
	
	public AlterEntry()
	{
		this("", null);
	}
	
	void onRegistered(AnimatedEntity<T> owner)
	{
		this.owner = owner;
		this.key = this.owner.key + postfix;
		if (this.unlocalizedName == null)
		{
			this.unlocalizedName = this.owner.unlocalizedName;
		}
	}
	
	protected T createPreviewEntity()
	{
		try 
		{
			EntityLiving entity = (EntityLiving) this.getOwner().entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().world);
			entity.world = Minecraft.getMinecraft().world;
			entity.setLocationAndAngles(0, 0, 0, 0, 0);
			entity.onInitialSpawn(entity.world.getDifficultyForLocation(entity.getPosition()), null);
			AlterEntry.registerPreviewEntity(entity);
			
			return (T) entity;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void setAnimate(boolean animate)
	{
		this.animate = animate;
		ArmorModelFactory.updateMutation();
	}
	
	public AlterEntry<T> setPreviewer(Previewer previewer)
	{
		this.previewer = previewer;
		return this;
	}
	
	public boolean isAnimated()
	{
		return this.animate;
	}
	
	public void toggleAnimated()
	{
		this.setAnimate(!this.animate);
	}
	
	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}
	
	public AnimatedEntity getOwner()
	{
		return owner;
	}
	
	public Previewer<?> getPreviewer()
	{
		return this.previewer;
	}
	
	public abstract LivingEntityData getDataForPreview();

	public String getLocalizedName()
	{
		return Lang.localize(this.unlocalizedName);
	}

	public String getKey()
	{
		return this.key;
	}
}
