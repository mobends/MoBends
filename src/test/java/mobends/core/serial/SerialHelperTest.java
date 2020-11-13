package mobends.core.serial;

import static org.junit.Assert.*;

import goblinbob.mobends.core.serial.*;
import org.junit.Before;
import org.junit.Test;

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

        public static SerializableMock deserialize(ISerialInput in)
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

        List<SerializableMock> deserializedList = SerialHelper.deserializeList(SerializableMock::deserialize, this.queue);
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

        Set<SerializableMock> deserializedSet = SerialHelper.deserializeSet(SerializableMock::deserialize, this.queue);
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

        SerializableMock[] deserializedArray = SerialHelper.deserializeArray(SerializableMock::deserialize, new SerializableMock[0], this.queue);
        assertArrayEquals(someArray, deserializedArray);
    }

    @Test
    public void storeNullableIsNull()
    {
        SerialHelper.serializeNullable(null, this.queue);
        assertNull(SerialHelper.deserializeNullable(SerializableMock::deserialize, this.queue));
    }

    @Test
    public void storeNullableNotNull()
    {
        SerializableMock object = new SerializableMock("Good morning Sunshine");

        SerialHelper.serializeNullable(object, this.queue);
        SerializableMock deserialized = SerialHelper.deserializeNullable(SerializableMock::deserialize, this.queue);

        assertEquals(object, deserialized);
    }
}
