package goblinbob.mobends.core.mutators;


import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.entity.EntityLivingBase;

@FunctionalInterface
public interface IMutatorFactory<E extends EntityLivingBase>
{
	
	Mutator<? extends LivingEntityData<E>, ? extends E, ?> createMutator();
	
}
