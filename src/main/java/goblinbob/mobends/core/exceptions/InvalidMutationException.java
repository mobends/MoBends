package goblinbob.mobends.core.exceptions;

import goblinbob.mobends.core.EntityBender;

public class InvalidMutationException extends RuntimeException
{
    private EntityBender<?> bender;

    public InvalidMutationException(String message, EntityBender<?> bender)
    {
        super(message);
        this.bender = bender;
    }

    public EntityBender<?> getBender()
    {
        return bender;
    }
}
