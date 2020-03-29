package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ZombieController;
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

	@Override
	public void onTicksRestart()
	{
		// No behaviour
	}

}