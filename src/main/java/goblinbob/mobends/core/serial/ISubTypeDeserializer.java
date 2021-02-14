package goblinbob.mobends.core.serial;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;

public interface ISubTypeDeserializer<T, D extends IEntityData>
{
    T deserialize(ISerialContext<D> context, String type, ISerialInput in) throws InvalidSubTypeException, IOException;
}
