package goblinbob.mobends.core;

import goblinbob.mobends.core.error.IErrorReportRegistry;
import goblinbob.mobends.core.error.TextStyle;
import goblinbob.mobends.core.exceptions.InvalidPackFormatException;

public class Core
{
    public void registerErrors(IErrorReportRegistry registry)
    {
        registry.register(InvalidPackFormatException.class, (stream, exception) -> {
            stream.print("A pack has been disabled due to it's wrong format: ");
            stream.print(exception.getPackName(), TextStyle.BOLD);
            stream.print(". Check the logs for more details...");
        });
    }
}
