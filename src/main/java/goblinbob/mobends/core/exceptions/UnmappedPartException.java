package goblinbob.mobends.core.exceptions;

public class UnmappedPartException extends Exception
{
    private String partName;

    public UnmappedPartException(String partName)
    {
        super();
        this.partName = partName;
    }

    public String getPartName()
    {
        return partName;
    }
}
