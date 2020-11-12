package goblinbob.mobends.core.exceptions;

public class InvalidPackFormatException extends Exception
{

    private final String packName;

    public InvalidPackFormatException(String packName, String message)
    {
        super(message);
        this.packName = packName;
    }

    public String getPackName()
    {
        return packName;
    }

}
