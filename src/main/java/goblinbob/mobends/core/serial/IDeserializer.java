package goblinbob.mobends.core.serial;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;

@FunctionalInterface
public interface IDeserializer<T, D extends IEntityData>
{
    T deserialize(ISerialContext<D> context, ISerialInput serialInput) throws IOException;
}
