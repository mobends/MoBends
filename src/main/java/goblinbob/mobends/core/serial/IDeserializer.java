package goblinbob.mobends.core.serial;

import java.io.IOException;

public interface IDeserializer<T>
{
    T deserialize(ISerialInput serialInput) throws IOException;
}
