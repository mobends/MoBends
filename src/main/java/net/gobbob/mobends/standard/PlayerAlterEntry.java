package net.gobbob.mobends.standard;

import java.lang.reflect.InvocationTargetException;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class PlayerAlterEntry extends AlterEntry
{

	public PlayerAlterEntry()
	{
		super();
	}
	
	@Override
	public EntityLivingBase getEntityForPreview()
	{
		return Minecraft.getMinecraft().player;
	}
	
}
