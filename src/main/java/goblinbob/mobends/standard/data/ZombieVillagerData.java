package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.monster.EntityZombieVillager;

public class ZombieVillagerData extends ZombieDataBase<EntityZombieVillager>
{

	/** The procedural ZombieVillagerController is kept as the parity reference; the entity animates from its animator asset. */
	private final KumoAnimatorController<ZombieVillagerData> controller = new KumoAnimatorController<>(ModStatics.MODID, "bends/animators/zombie_villager.json");
	
	public ZombieVillagerData(EntityZombieVillager entity)
	{
		super(entity);
	}
	
	@Override
	public KumoAnimatorController<ZombieVillagerData> getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
		// No behaviour
	}

}
