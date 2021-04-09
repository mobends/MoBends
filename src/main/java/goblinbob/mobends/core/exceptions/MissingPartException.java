package goblinbob.mobends.core.exceptions;

import goblinbob.mobends.core.EntityBender;

public class MissingPartException extends InvalidMutationException
{
    private String partName;
    private String invalidFieldName;

    public MissingPartException(EntityBender<?, ?> bender, String partName, String invalidFieldName)
    {
        super(String.format("Failed to find part '%s' under '%s' of bender '%s'", partName, invalidFieldName, bender.getKey()), bender);
        this.partName = partName;
        this.invalidFieldName = invalidFieldName;
    }

    public String getPartName()
    {
        return partName;
    }

    public String getInvalidFieldName()
    {
        return invalidFieldName;
    }
}
