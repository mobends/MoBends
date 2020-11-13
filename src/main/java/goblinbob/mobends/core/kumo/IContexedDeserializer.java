package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialInput;

import java.io.IOException;

public interface IContexedDeserializer<T>
{
    T deserialize(ISerialInput serialInput, ISerialContext context) throws IOException;
}
