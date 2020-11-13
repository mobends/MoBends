package goblinbob.mobends.core.serial;

public interface IDeserializer<T>
{
    T deserialize(ISerialInput serialInput);
}
