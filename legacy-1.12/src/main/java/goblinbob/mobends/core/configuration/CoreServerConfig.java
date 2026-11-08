package goblinbob.mobends.core.configuration;

import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;

import java.io.File;

public class CoreServerConfig extends CoreConfig
{
    private static final String CATEGORY_SERVER = "Server";

    public CoreServerConfig(File file)
    {
        super(file);

        final Iterable<SharedProperty<?>> props = NetworkConfiguration.instance.getSharedConfig().getProperties();

        for (SharedProperty<?> prop : props)
        {
            prop.updateWithConfig(configuration, CATEGORY_SERVER);
        }

        // This should create default props on the first run.
        configuration.save();
    }

    @Override
    public void save()
    {
        // No config to save.
    }
}
