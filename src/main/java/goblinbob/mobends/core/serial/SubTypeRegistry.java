package goblinbob.mobends.core.serial;

import java.util.HashMap;
import java.util.Map;

public class SubTypeRegistry<T> implements ISubTypeRegistry<T>, ISubTypeDeserializer<T>
{

    private Map<String, IDeserializer<T>> keyToDeserializerMap = new HashMap<>();

    @Override
    public void register(String type, IDeserializer<T> deserializer)
    {
        keyToDeserializerMap.put(type, deserializer);
    }

    @Override
    public T deserialize(String type, ISerialInput in) throws InvalidSubTypeException
    {
        if (keyToDeserializerMap.containsKey(type))
        {
            return keyToDeserializerMap.get(type).deserialize(in);
        }

        throw new InvalidSubTypeException(type);
    }

}
