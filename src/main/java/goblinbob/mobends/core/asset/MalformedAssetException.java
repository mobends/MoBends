package goblinbob.mobends.core.asset;

public class MalformedAssetException extends Exception
{
    public MalformedAssetException(String msg)
    {
        super(msg);
    }

    public MalformedAssetException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
