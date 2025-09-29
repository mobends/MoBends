package goblinbob.mobends.core.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SerialHelper {

    public static String readChar(DataInputStream stream, int length)
        throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(length);
        for (int i = 0; i < length; ++i) {
            buffer.write(stream.readByte());
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }
}
