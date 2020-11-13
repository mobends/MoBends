package goblinbob.mobends.core.serial;

public interface ISubTypeDeserializer<T>
{
    T deserialize(String type, ISerialInput in) throws InvalidSubTypeException;
}
