package goblinbob.mobends.core.serial;

public interface ISerialOutput
{
    void writeBoolean(boolean value);

    void writeByte(byte value);

    void writeInt(int value);

    void writeFloat(float value);

    void writeString(String value);
}
