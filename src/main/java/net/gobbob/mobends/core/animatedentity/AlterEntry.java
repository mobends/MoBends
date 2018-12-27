package net.gobbob.mobends.core.animatedentity;

import java.lang.reflect.InvocationTargetException;

import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

public class AlterEntry
{
	private String name;
	private String displayName;
	public AnimatedEntity owner;
	private boolean animate;
	
	public AlterEntry(AnimatedEntity owner, String displayName)
	{
		this.owner = owner;
		this.name = owner.getName();
		this.displayName = displayName;
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
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public String getName() {
		return name;
	}
	
	public AnimatedEntity getOwner() {
		return owner;
	}
	
	public EntityLivingBase getEntity() {
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
}