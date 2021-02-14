package goblinbob.mobends.core.exceptions;

public class AnimationRuntimeException extends RuntimeException
{
    public AnimationRuntimeException(String message)
    {
        super(message);
    }

    public AnimationRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
