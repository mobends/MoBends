package goblinbob.mobends.core.configuration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public abstract class CoreConfig
{

    // Server
    private static final String CATEGORY_SERVER = "Server";
    private static final String PROP_ALLOW_MODEL_SCALING = "AllowModelScaling";
    private static final String PROP_ALLOW_MODEL_SCALING_DESC = "Does the server allow scaling of the player model more than the normal size?";

    protected Configuration configuration;

    CoreConfig(File file)
    {
        configuration = new Configuration(file);
    }

    public abstract void save();

    public boolean isModelScalingAllowed()
    {
        return configuration.get(CATEGORY_SERVER, PROP_ALLOW_MODEL_SCALING, false, PROP_ALLOW_MODEL_SCALING_DESC).getBoolean();
    }

}
