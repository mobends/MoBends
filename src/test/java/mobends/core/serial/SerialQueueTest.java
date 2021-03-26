package mobends.core.serial;

import goblinbob.mobends.core.serial.SerialQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SerialQueueTest
{
    private SerialQueue queue;
    private Random rand;
    private final float PRECISION_ERROR = 0.0001F;

    @Before
    public void init()
    {
        this.queue = new SerialQueue(1024);
        this.rand = new Random();
    }

    @Test
    public void storeBoolean()
    {
        boolean value = rand.nextBoolean();
        queue.writeBoolean(value);

        assertEquals(value, queue.readBoolean());
    }

    @Test
    public void storeByte()
    {
        byte value = (byte) rand.nextInt();
        queue.writeByte(value);

        assertEquals(value, queue.readByte());
    }

    @Test
    public void storeInt()
    {
        int value = rand.nextInt();
        queue.writeInt(value);
        assertEquals(value, queue.readInt());
    }

    @Test
    public void storeFloat()
    {
        float value = rand.nextFloat();
        queue.writeFloat(value);
        assertEquals(value, queue.readFloat(), PRECISION_ERROR);
    }

    @Test
    public void storeString()
    {
        String value = "Sample text fo lifee  ";
        queue.writeString(value);
        assertEquals(value, queue.readString());
    }

    @Test
    public void storeSequence()
    {
        int age = rand.nextInt();
        String name = "James";
        float percentage = rand.nextFloat();

        queue.writeInt(age);
        queue.writeString(name);
        queue.writeFloat(percentage);

        assertEquals(age, queue.readInt());
        assertEquals(name, queue.readString());
        assertEquals(percentage, queue.readFloat(), PRECISION_ERROR);
    }
}
