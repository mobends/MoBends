package goblinbob.mobends.core.configuration;

import net.minecraftforge.common.config.Property;

import java.io.File;

public class CoreServerConfig extends CoreConfig
{

    // Server
    private static final String CATEGORY_SERVER = "Server";
    private static final String PROP_ALLOW_MODEL_SCALING = "AllowModelScaling";
    private static final String PROP_ALLOW_MODEL_SCALING_DESC = "Does the server allow scaling of the player model more than the normal size?";

    public CoreServerConfig(File file)
    {
        super(file);
    }

    @Override
    public void save()
    {
        // No config to save.
        configuration.save();
    }

    private Property getAllowModelScalingProp()
    {
        return configuration.get(CATEGORY_SERVER, PROP_ALLOW_MODEL_SCALING, false, PROP_ALLOW_MODEL_SCALING_DESC);
    }

    public void createDefaultConfig()
    {
        getAllowModelScalingProp();
        configuration.save();
    }

    public boolean isModelScalingAllowed()
    {
        return getAllowModelScalingProp().getBoolean();
    }

}
