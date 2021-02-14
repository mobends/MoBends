package goblinbob.mobends.core.exceptions;

public class UnknownPropertyException extends RuntimeException
{
    private final String propertyKey;

    public UnknownPropertyException(String propertyKey, String message)
    {
        super(message);
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }
}
