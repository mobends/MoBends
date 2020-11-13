package goblinbob.mobends.core.serial;

public class InvalidSubTypeException extends RuntimeException
{
    private final String subType;

    public InvalidSubTypeException(String subType)
    {
        super(String.format("SubType couldn't be found in the registry: '%s'.", subType));
        this.subType = subType;
    }

    public String getSubType()
    {
        return this.subType;
    }

}
