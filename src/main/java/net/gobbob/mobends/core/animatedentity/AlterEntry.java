package net.gobbob.mobends.core.animatedentity;

import java.lang.reflect.InvocationTargetException;

import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

public class AlterEntry
{
	String key;
	String unlocalizedName;
	String postfix;
	public AnimatedEntity owner;
	private boolean animate;
	
	public AlterEntry(String postfix, String unlocalizedName)
	{
		this.postfix = postfix;
		this.unlocalizedName = unlocalizedName;
	}
	
	public AlterEntry()
	{
		this("", null);
	}
	
	void onRegistered(AnimatedEntity owner)
	{
		this.owner = owner;
		this.key = this.owner.key + postfix;
		if (this.unlocalizedName == null)
		{
			this.unlocalizedName = this.owner.unlocalizedName;
		}
	}
	
	public void setAnimate(boolean animate)
	{
		this.animate = animate;
		ArmorModelFactory.updateMutation();
	}
	
	public boolean isAnimated()
	{
		return this.animate;
	}
	
	public void toggleAnimated() {
		this.setAnimate(!this.animate);
	}
	
	public String getUnlocalizedName() {
		return this.unlocalizedName;
	}
	
	public AnimatedEntity getOwner() {
		return owner;
	}
	
	public EntityLivingBase getEntityForPreview()
	{
		EntityLiving entity = null;
		try {
			entity = (EntityLiving) this.getOwner().entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().world);
			entity.world = Minecraft.getMinecraft().world;
			entity.setLocationAndAngles(0, 0, 0, 0, 0);
			entity.onInitialSpawn(entity.world.getDifficultyForLocation(entity.getPosition()), null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return entity;
	}

	public String getLocalizedName()
	{
		return Lang.localize(this.unlocalizedName);
	}

	public String getKey()
	{
		return this.key;
	}
}