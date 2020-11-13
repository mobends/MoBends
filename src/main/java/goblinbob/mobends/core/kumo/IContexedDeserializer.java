package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.serial.ISerialInput;

public interface IContexedDeserializer<T>
{
    T deserialize(ISerialInput serialInput, ISerialContext context);
}
