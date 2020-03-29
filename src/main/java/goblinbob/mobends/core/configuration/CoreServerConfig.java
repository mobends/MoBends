package goblinbob.mobends.core.configuration;

import java.io.File;

public class CoreServerConfig extends CoreConfig
{

    public CoreServerConfig(File file)
    {
        super(file);
    }

    @Override
    public void save()
    {
        // No config to save.
    }

}
