package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.PigZombieController;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieData extends BipedEntityData<EntityPigZombie>
{
	
	private final PigZombieController controller = new PigZombieController();
	
	public PigZombieData(EntityPigZombie entity)
	{
		super(entity);
	}

	@Override
	public PigZombieController getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
		// No behaviour
	}

}
