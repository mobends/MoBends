package goblinbob.mobends.core.kumo.state.template;

/** An animator asset that can't be instanced, or that failed while animating. */
public class MalformedKumoTemplateException extends Exception
{

    public MalformedKumoTemplateException(String message)
    {
        super(message);
    }

    public MalformedKumoTemplateException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
