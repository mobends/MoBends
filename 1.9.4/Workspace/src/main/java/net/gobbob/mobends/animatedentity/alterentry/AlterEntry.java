package net.gobbob.mobends.animatedentity.alterentry;

import java.lang.reflect.InvocationTargetException;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;

public class AlterEntry {
	private String displayName;
	public int animatedEntityIndex;
	public String id;
	private boolean animate;
	
	public AlterEntry(String id, String displayName) {
		this.animatedEntityIndex = AnimatedEntity.animatedEntities.size();
		this.id = id;
		this.displayName = displayName;
	}
	
	public void setAnimate(boolean animate) {
		this.animate = animate;
	}
	
	public boolean isAnimated() {
		return this.animate;
	}
	
	public void toggleAnimated() {
		this.animate = !this.animate;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public String getID() {
		return this.id;
	}
	
	public int getAnimatedEntityIndex() {
		return this.animatedEntityIndex;
	}
	
	public AnimatedEntity getOwner() {
		return AnimatedEntity.animatedEntities.get(this.animatedEntityIndex);
	}
	
	public EntityLivingBase getEntity() {
		EntityLiving entity = null;
		try {
			entity = (EntityLiving) this.getOwner().entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().theWorld);
			entity.worldObj = Minecraft.getMinecraft().theWorld;
			entity.setLocationAndAngles(0, 0, 0, 0, 0);
			entity.onInitialSpawn(entity.worldObj.getDifficultyForLocation(entity.getPosition()), null);
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