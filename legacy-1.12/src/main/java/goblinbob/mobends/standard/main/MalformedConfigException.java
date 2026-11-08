package goblinbob.mobends.standard.main;

public class MalformedConfigException extends RuntimeException
{
    public MalformedConfigException(String message)
    {
        super(message);
    }

    public MalformedConfigException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
