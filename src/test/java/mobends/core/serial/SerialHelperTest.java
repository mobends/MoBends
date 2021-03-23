package mobends.core.serial;

import static org.junit.Assert.*;

import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerialHelperTest
{

    private static class SerializableMock implements ISerializable
    {
        private String value;

        public SerializableMock(String value)
        {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SerializableMock))
                return false;

            SerializableMock other = (SerializableMock) obj;
            return other.value.equals(this.value);
        }

        @Override
        public int hashCode()
        {
            return value.hashCode();
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            out.writeString(this.value);
        }

        public static SerializableMock deserialize(ISerialContext<?> context, ISerialInput in) throws IOException
        {
            return new SerializableMock(in.readString());
        }
    }

    private SerialQueue queue;

    @Before
    public void init()
    {
        this.queue = new SerialQueue(1024);
    }

    @Test
    public void storeListTest()
    {
        List<SerializableMock> someList = new ArrayList<>();
        someList.add(new SerializableMock("Hello there!"));
        someList.add(new SerializableMock("What's up slappers"));

        SerialHelper.serializeList(someList, this.queue);

        List<SerializableMock> deserializedList = null;
        try
        {
            deserializedList = SerialHelper.deserializeList(null, SerializableMock::deserialize, this.queue);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assertEquals(someList, deserializedList);
    }

    @Test
    public void storeSetTest()
    {
        Set<SerializableMock> someSet = new HashSet<>();
        someSet.add(new SerializableMock("Hello there!"));
        someSet.add(new SerializableMock("What's up slappers"));
        someSet.add(new SerializableMock("What's up slappers"));

        SerialHelper.serializeSet(someSet, this.queue);

        Set<SerializableMock> deserializedSet = null;
        try
        {
            deserializedSet = SerialHelper.deserializeSet(null, SerializableMock::deserialize, this.queue);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assertEquals(someSet, deserializedSet);
    }

    @Test
    public void storeArrayTest()
    {
        SerializableMock[] someArray = new SerializableMock[] {
                new SerializableMock("Hello there!"),
                new SerializableMock("What's up slappers")
        };

        SerialHelper.serializeArray(someArray, this.queue);

        SerializableMock[] deserializedArray = new SerializableMock[0];
        try
        {
            deserializedArray = SerialHelper.deserializeArray(null, SerializableMock::deserialize, new SerializableMock[0], this.queue);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assertArrayEquals(someArray, deserializedArray);
    }

    @Test
    public void storeNullableIsNull()
    {
        SerialHelper.serializeNullable(null, this.queue);
        try
        {
            assertNull(SerialHelper.deserializeNullable(null, SerializableMock::deserialize, this.queue));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void storeNullableNotNull()
    {
        SerializableMock object = new SerializableMock("Good morning Sunshine");

        SerialHelper.serializeNullable(object, this.queue);
        SerializableMock deserialized = null;
        try
        {
            deserialized = SerialHelper.deserializeNullable(null, SerializableMock::deserialize, this.queue);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        assertEquals(object, deserialized);
    }
}
