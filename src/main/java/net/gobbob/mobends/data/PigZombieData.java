package net.gobbob.mobends.data;

import net.gobbob.mobends.animation.controller.PigZombieController;
import net.gobbob.mobends.animation.controller.ZombieController;
import net.minecraft.entity.Entity;

public class PigZombieData extends BipedEntityData
{
	public PigZombieData(Entity entity)
	{
		super(entity);
		this.controller = new PigZombieController();
	}

	@Override
	public void onTicksRestart()
	{
	}
}
