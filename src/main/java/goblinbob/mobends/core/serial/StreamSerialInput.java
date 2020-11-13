package goblinbob.mobends.core.serial;

import org.apache.http.util.ByteArrayBuffer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StreamSerialInput implements ISerialInput
{

    private DataInputStream dataInputStream;

    public StreamSerialInput(InputStream stream)
    {
        if (stream instanceof DataInputStream)
            this.dataInputStream = (DataInputStream) stream;
        else
            this.dataInputStream = new DataInputStream(stream);
    }

    @Override
    public boolean readBoolean() throws IOException
    {
        return dataInputStream.readBoolean();
    }

    @Override
    public byte readByte() throws IOException
    {
        return dataInputStream.readByte();
    }

    @Override
    public int readInt() throws IOException
    {
        return dataInputStream.readInt();
    }

    @Override
    public float readFloat() throws IOException
    {
        return dataInputStream.readFloat();
    }

    @Override
    public String readString() throws IOException
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
}
