package goblinbob.mobends.core.serial;

import java.io.IOException;
import java.util.*;

public class SerialHelper
{
    public static <T extends ISerializable> void serializeNullable(T nullable, ISerialOutput out)
    {
        if (nullable == null)
        {
            out.writeByte((byte) 0);
        }
        else
        {
            out.writeByte((byte) 1);
            nullable.serialize(out);
        }
    }

    public static <T extends ISerializable> void serializeList(List<T> listToSerialize, ISerialOutput out)
    {
        out.writeInt(listToSerialize.size());

        for (T element : listToSerialize)
        {
            element.serialize(out);
        }
    }

    public static <T extends ISerializable> void serializeSet(Set<T> setToSerialize, ISerialOutput out)
    {
        out.writeInt(setToSerialize.size());

        for (T element : setToSerialize)
        {
            element.serialize(out);
        }
    }

    public static <T extends ISerializable> void serializeArray(T[] arrayToSerialize, ISerialOutput out)
    {
        out.writeInt(arrayToSerialize.length);

        for (T element : arrayToSerialize)
        {
            element.serialize(out);
        }
    }

    public static <T extends ISerializable> T deserializeNullable(IDeserializer<T> deserializer, ISerialInput in) throws IOException
    {
        boolean isItNull = in.readByte() == 0;

        if (isItNull)
        {
            return null;
        }

        return deserializer.deserialize(in);
    }

    public static <T> List<T> deserializeList(IDeserializer<T> deserializer, ISerialInput in) throws IOException
    {
        List<T> list = new ArrayList<>();
        int sizeOfTheList = in.readInt();

        for (int i = 0; i < sizeOfTheList; ++i)
        {
            list.add(deserializer.deserialize(in));
        }

        return list;
    }

    public static <T> Set<T> deserializeSet(IDeserializer<T> deserializer, ISerialInput in) throws IOException
    {
        Set<T> set = new HashSet<>();
        int sizeOfTheList = in.readInt();

        for (int i = 0; i < sizeOfTheList; ++i)
        {
            set.add(deserializer.deserialize(in));
        }

        return set;
    }

    public static <T> T[] deserializeArray(IDeserializer<T> deserializer, T[] emptyArray, ISerialInput in) throws IOException
    {
        int sizeOfTheArray = in.readInt();
        T[] list = Arrays.copyOf(emptyArray, sizeOfTheArray);

        for (int i = 0; i < sizeOfTheArray; ++i)
        {
            list[i] = deserializer.deserialize(in);
        }

        return list;
    }

    public static float[] deserializeFloats(int amountOfFloats, ISerialInput in) throws IOException
    {
        float[] floats = new float[amountOfFloats];

        for (int i = 0; i < amountOfFloats; ++i)
        {
            floats[i] = in.readFloat();
        }

        return floats;
    }
}
