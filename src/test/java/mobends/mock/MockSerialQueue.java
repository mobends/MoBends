package mobends.mock;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import org.junit.Assert;

import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.Queue;

public class MockSerialQueue implements ISerialInput, ISerialOutput
{
    private Queue<QueueElement> innerQueue = new LinkedList<>();

    private enum QueueElementType
    {
        BOOLEAN,
        BYTE,
        INT,
        FLOAT,
        STRING,
    }

    private static class QueueElement
    {
        private QueueElementType type;
        private Object value;

        public QueueElement(QueueElementType type, Object value)
        {
            this.type = type;
            this.value = value;
        }
    }

    private Object read(QueueElementType expectedType)
    {
        QueueElement element = this.innerQueue.poll();
        Assert.assertEquals(expectedType, element.type);
        return element.value;
    }

    @Override
    public boolean readBoolean() throws BufferUnderflowException
    {
        return (boolean) read(QueueElementType.BOOLEAN);
    }

    @Override
    public byte readByte() throws BufferUnderflowException
    {
        return (byte) read(QueueElementType.BYTE);
    }

    @Override
    public int readInt() throws BufferUnderflowException
    {
        return (int) read(QueueElementType.INT);
    }

    @Override
    public float readFloat() throws BufferUnderflowException
    {
        return (float) read(QueueElementType.FLOAT);
    }

    @Override
    public String readString() throws BufferUnderflowException
    {
        return (String) read(QueueElementType.STRING);
    }

    @Override
    public void writeBoolean(boolean value)
    {
        this.innerQueue.offer(new QueueElement(QueueElementType.BOOLEAN, value));
    }

    @Override
    public void writeByte(byte value)
    {
        this.innerQueue.offer(new QueueElement(QueueElementType.BYTE, value));
    }

    @Override
    public void writeInt(int value)
    {
        this.innerQueue.offer(new QueueElement(QueueElementType.INT, value));
    }

    @Override
    public void writeFloat(float value)
    {
        this.innerQueue.offer(new QueueElement(QueueElementType.FLOAT, value));
    }

    @Override
    public void writeString(String value)
    {
        this.innerQueue.offer(new QueueElement(QueueElementType.STRING, value));
    }
}
