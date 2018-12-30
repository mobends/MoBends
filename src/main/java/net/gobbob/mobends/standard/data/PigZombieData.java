package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.standard.animation.controller.PigZombieController;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieData extends BipedEntityData<EntityPigZombie>
{
	
	final PigZombieController controller = new PigZombieController();
	
	public PigZombieData(EntityPigZombie entity)
	{
		super(entity);
	}

	@Override
	public PigZombieController getController()
	{
		return controller;
	}
}
