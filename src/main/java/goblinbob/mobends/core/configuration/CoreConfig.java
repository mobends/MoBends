package goblinbob.mobends.core.configuration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public abstract class CoreConfig
{
    protected Configuration configuration;

    CoreConfig(File file)
    {
        configuration = new Configuration(file);
    }

    public abstract void save();
}
