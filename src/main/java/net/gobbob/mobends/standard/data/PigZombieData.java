package net.gobbob.mobends.data;

import net.gobbob.mobends.animation.controller.PigZombieController;
import net.gobbob.mobends.animation.controller.ZombieController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieData extends BipedEntityData<PigZombieData, EntityPigZombie>
{
	public PigZombieData(EntityPigZombie entity)
	{
		super(entity);
		this.controller = new PigZombieController();
	}
}
