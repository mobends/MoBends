package goblinbob.mobends.core;

import goblinbob.mobends.forge.EntityData;

public interface IEntityDataRepository<C>
{
    EntityData getOrMakeData(C context, EntityBender<C> bender);
}
