package goblinbob.mobends.standard.data;

import net.minecraft.entity.monster.EntityZombie;


/**
 * The Java zombies' data. Their animation set and walking style are their model definition's
 * entity scope (bends/models/zombie.json), as for the defined zombies.
 */
public abstract class ZombieDataBase<E extends EntityZombie> extends BipedEntityData<E>
{

	public ZombieDataBase(E entity)
	{
		super(entity);
	}

}
