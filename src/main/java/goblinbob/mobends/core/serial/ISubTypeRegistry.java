package goblinbob.mobends.core.serial;

public interface ISubTypeRegistry<T>
{
    void register(String type, IDeserializer<T> deserializer);
}
