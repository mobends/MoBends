package goblinbob.mobends.core;

import goblinbob.mobends.forge.EntityData;

public interface IEntityDataRepository<C, R extends IBenderResources>
{
    EntityData getOrMakeData(C context, EntityBender<C, R> bender);
}
