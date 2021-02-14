package goblinbob.mobends.core.serial;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SubTypeRegistry<T, D extends IEntityData> implements ISubTypeRegistry<T, D>, ISubTypeDeserializer<T, D>
{
    private Map<String, ISubTypeDeserializer<T, D>> keyToDeserializerMap = new HashMap<>();

    @Override
    public void register(String type, ISubTypeDeserializer<T, D> deserializer)
    {
        keyToDeserializerMap.put(type, deserializer);
    }

    @Override
    public T deserialize(ISerialContext<D> context, String type, ISerialInput in) throws InvalidSubTypeException, IOException
    {
        if (keyToDeserializerMap.containsKey(type))
        {
            return keyToDeserializerMap.get(type).deserialize(context, type, in);
        }

        throw new InvalidSubTypeException(type);
    }
}
