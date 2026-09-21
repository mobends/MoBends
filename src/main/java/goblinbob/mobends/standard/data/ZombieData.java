package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieData extends ZombieDataBase<EntityZombie>
{
	
	/** The procedural ZombieController is kept as the parity reference; the entity animates from its animator asset. */
	private final KumoAnimatorController<ZombieData> controller = new KumoAnimatorController<>(ModStatics.MODID, "bends/animators/zombie.json");
	
	public ZombieData(EntityZombie entity)
	{
		super(entity);
	}
	
	@Override
	public KumoAnimatorController<ZombieData> getController()
	{
		return this.controller;
	}

	@Override
	public void onTicksRestart()
	{
		// No behaviour
	}

}