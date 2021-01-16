package goblinbob.mobends.core;

import net.minecraft.entity.LivingEntity;

public interface IEntityBenderProvider<C>
{
    EntityBender<C> getBenderForEntity(LivingEntity entity);
}
