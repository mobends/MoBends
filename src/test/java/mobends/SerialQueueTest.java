package mobends;

import static org.junit.Assert.*;

import goblinbob.mobends.core.serial.SerialQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class SerialQueueTest
{
    private SerialQueue buffer;
    private Random rand;
    private final float PRECISION_ERROR = 0.0001F;

    @Before
    public void init()
    {
        this.buffer = new SerialQueue(1024);
        this.rand = new Random();
    }

    @Test
    public void storeInt()
    {
        int value = rand.nextInt();
        buffer.writeInt(value);
        assertEquals(value, buffer.readInt());
    }

    @Test
    public void storeFloat()
    {
        float value = rand.nextFloat();
        buffer.writeFloat(value);
        assertEquals(value, buffer.readFloat(), PRECISION_ERROR);
    }

    @Test
    public void storeString()
    {
        String value = "Sample text fo lifee  ";
        buffer.writeString(value);
        assertEquals(value, buffer.readString());
    }
}
