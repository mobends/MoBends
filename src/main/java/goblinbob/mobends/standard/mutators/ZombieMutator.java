package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.standard.data.ZombieData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieMutator extends BipedMutator<ZombieData, EntityZombie, ModelZombie>
{

	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelZombie);
	}

}
