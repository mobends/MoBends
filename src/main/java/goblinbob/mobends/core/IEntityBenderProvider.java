package goblinbob.mobends.core;

import net.minecraft.entity.LivingEntity;

public interface IEntityBenderProvider<C, R extends IBenderResources>
{
    EntityBender<C, R> getBenderForEntity(LivingEntity entity);
}
