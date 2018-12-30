package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.standard.animation.controller.ZombieVillagerController;
import net.minecraft.entity.monster.EntityZombieVillager;

public class ZombieVillagerData extends ZombieDataBase<EntityZombieVillager>
{

	private final ZombieVillagerController controller = new ZombieVillagerController();
	
	public ZombieVillagerData(EntityZombieVillager entity)
	{
		super(entity);
	}
	
	@Override
	public ZombieVillagerController getController()
	{
		return controller;
	}
	
}
