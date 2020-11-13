package goblinbob.mobends.core.serial;

import java.nio.BufferUnderflowException;

public interface ISerialInput
{

    boolean readBoolean() throws BufferUnderflowException;

    byte readByte() throws BufferUnderflowException;

    int readInt() throws BufferUnderflowException;

    float readFloat() throws BufferUnderflowException;

    String readString() throws BufferUnderflowException;

}
