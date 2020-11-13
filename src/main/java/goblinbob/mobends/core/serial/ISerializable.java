package goblinbob.mobends.core.serial;

@FunctionalInterface
public interface ISerializable
{
    void serialize(ISerialOutput out);
}
