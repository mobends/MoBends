package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieMutator extends BipedMutator<PigZombieData, EntityPigZombie, ModelZombie>
{

	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelZombie);
	}

}
