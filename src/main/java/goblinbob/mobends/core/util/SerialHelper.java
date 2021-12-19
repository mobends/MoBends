package goblinbob.mobends.core.util;

import org.apache.http.util.ByteArrayBuffer;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SerialHelper
{
    public static String readChar(DataInputStream stream, int length) throws IOException
    {
        ByteArrayBuffer buffer = new ByteArrayBuffer(length);
        for (int i = 0; i < length; ++i)
        {
            buffer.append(stream.readByte());
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
