package goblinbob.mobends.core.serial;

import java.io.IOException;

public interface ISubTypeDeserializer<T>
{
    T deserialize(String type, ISerialInput in) throws InvalidSubTypeException, IOException;
}
