package goblinbob.mobends.core.mutation;

public class PuppeteerException extends Exception
{
    public PuppeteerException(String message)
    {
        super(message);
    }

    public PuppeteerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
