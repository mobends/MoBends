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
	private final AnimatedEntity<?> owner;
	private boolean animate;
	
	public AlterEntry(AnimatedEntity<?> owner)
	{
		this.owner = owner;
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
		return owner.getDisplayName();
	}
	
	public String getName() {
		return owner.getName();
	}
	
	public AnimatedEntity<?> getOwner() {
		return owner;
	}
	
	public EntityLivingBase getEntity() {
		EntityLiving entity = null;
		try {
			entity = (EntityLiving) this.getOwner().entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().world);
			entity.world = Minecraft.getMinecraft().world;
			entity.setLocationAndAngles(0, 0, 0, 0, 0);
			entity.onInitialSpawn(entity.world.getDifficultyForLocation(entity.getPosition()), null);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
}