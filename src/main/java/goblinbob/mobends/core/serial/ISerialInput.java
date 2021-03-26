package goblinbob.mobends.core.serial;

import java.io.IOException;

public interface ISerialInput
{

    boolean readBoolean() throws IOException;

    byte readByte() throws IOException;

    int readInt() throws IOException;

    float readFloat() throws IOException;

    String readString() throws IOException;

}
