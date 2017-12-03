package net.gobbob.mobends.animatedentity.alterentry;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AlterEntryWitherSkeleton extends AlterEntry{
	public AlterEntryWitherSkeleton(String id, String displayName) {
		super(id, displayName);
	}
	
	public EntityLivingBase getEntity() {
		EntityLiving entity = null;
		try {
			entity = (EntityLiving) this.getOwner().entityClass.getConstructor(World.class).newInstance(Minecraft.getMinecraft().theWorld);
			entity.worldObj = Minecraft.getMinecraft().theWorld;
			entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.stone_sword));
			((EntitySkeleton)entity).setSkeletonType(1);
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