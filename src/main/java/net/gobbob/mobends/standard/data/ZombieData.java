package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.standard.animation.controller.ZombieController;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieData extends ZombieDataBase<EntityZombie>
{
	
	private final ZombieController controller = new ZombieController();
	
	public ZombieData(EntityZombie entity)
	{
		super(entity);
	}
	
	@Override
	public ZombieController getController()
	{
		return this.controller;
	}
	
}