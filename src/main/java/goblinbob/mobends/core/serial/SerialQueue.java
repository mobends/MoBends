package goblinbob.mobends.core.serial;

import org.apache.http.util.ByteArrayBuffer;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerialQueue implements ISerialOutput, ISerialInput
{

    private ByteBuffer buffer;
    private int tail = 0;

    public SerialQueue(int capacity)
    {
        this.buffer = ByteBuffer.allocate(capacity);
    }

    @Override
    public boolean readBoolean() throws BufferUnderflowException
    {
        return this.readByte() != 0;
    }

    @Override
    public byte readByte() throws BufferUnderflowException
    {
        return buffer.get(tail++);
    }

    @Override
    public int readInt()
    {
        int value = buffer.getInt(tail);
        tail += 4;
        return value;
    }

    @Override
    public float readFloat()
    {
        float value = buffer.getFloat(tail);
        tail += 4;
        return value;
    }

    @Override
    public String readString() throws BufferUnderflowException
    {
        ByteArrayBuffer stringBuffer = new ByteArrayBuffer(16);

        byte character = this.readByte();
        while (character != '\0')
        {
            stringBuffer.append(character);
            character = this.readByte();
        }

        return new String(stringBuffer.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public void writeBoolean(boolean value)
    {
        buffer.put((byte) (value ? 1 : 0));
    }

    @Override
    public void writeByte(byte value)
    {
        buffer.put(value);
    }

    @Override
    public void writeInt(int value)
    {
        buffer.putInt(value);
    }

    @Override
    public void writeFloat(float value)
    {
        buffer.putFloat(value);
    }

    @Override
    public void writeString(String value)
    {
        buffer.put(value.getBytes());
        buffer.put((byte) 0);
    }
}
