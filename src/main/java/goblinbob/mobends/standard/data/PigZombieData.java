package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieData extends BipedEntityData<EntityPigZombie>
{
	
	private final KumoAnimatorController<PigZombieData> controller = new KumoAnimatorController<>(ModStatics.MODID, "bends/animators/pig_zombie.json");
	
	public PigZombieData(EntityPigZombie entity)
	{
		super(entity);
	}

	@Override
	public KumoAnimatorController<PigZombieData> getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
		// No behaviour
	}

}
