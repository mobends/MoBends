package goblinbob.mobends.core.serial;

import goblinbob.mobends.core.data.IEntityData;

public interface ISubTypeRegistry<T, D extends IEntityData>
{
    void register(String type, ISubTypeDeserializer<T, D> deserializer);
}
