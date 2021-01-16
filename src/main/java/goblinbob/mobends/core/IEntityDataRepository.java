package goblinbob.mobends.core;

import goblinbob.mobends.core.data.EntityData;

public interface IEntityDataRepository<C>
{
    EntityData getOrMakeData(C context);
}
