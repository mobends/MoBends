package goblinbob.mobends.core.serial;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ISerialContext;

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

    public static <E extends Enum<E>> void serializeEnum(E value, ISerialOutput out)
    {
        out.writeByte((byte) value.ordinal());
    }

    public static <T extends ISerializable, D extends IEntityData> T deserializeNullable(ISerialContext<D> context, IDeserializer<T, D> deserializer, ISerialInput in) throws IOException
    {
        boolean isItNull = in.readByte() == 0;

        if (isItNull)
        {
            return null;
        }

        return deserializer.deserialize(context, in);
    }

    public static <T, D extends IEntityData> List<T> deserializeList(ISerialContext<D> context, IDeserializer<T, D> deserializer, ISerialInput in) throws IOException
    {
        List<T> list = new ArrayList<>();
        int sizeOfTheList = in.readInt();

        for (int i = 0; i < sizeOfTheList; ++i)
        {
            list.add(deserializer.deserialize(context, in));
        }

        return list;
    }

    public static <T, D extends IEntityData> Set<T> deserializeSet(ISerialContext<D> context, IDeserializer<T, D> deserializer, ISerialInput in) throws IOException
    {
        Set<T> set = new HashSet<>();
        int sizeOfTheList = in.readInt();

        for (int i = 0; i < sizeOfTheList; ++i)
        {
            set.add(deserializer.deserialize(context, in));
        }

        return set;
    }

    public static <T, D extends IEntityData> T[] deserializeArray(ISerialContext<D> context, IDeserializer<T, D> deserializer, T[] emptyArray, ISerialInput in) throws IOException
    {
        int sizeOfTheArray = in.readInt();
        T[] list = Arrays.copyOf(emptyArray, sizeOfTheArray);

        for (int i = 0; i < sizeOfTheArray; ++i)
        {
            list[i] = deserializer.deserialize(context, in);
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

    public static <E extends Enum<E>> E deserializeEnum(E[] values, ISerialInput in) throws IOException
    {
        int index = in.readByte();

        if (index < 0 || index >= values.length)
        {
            return null;
        }

        return values[index];
    }
}
