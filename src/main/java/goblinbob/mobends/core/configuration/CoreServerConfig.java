package goblinbob.mobends.core.configuration;

import net.minecraftforge.common.config.Property;

import java.io.File;

public class CoreServerConfig extends CoreConfig
{

    // Server
    private static final String CATEGORY_SERVER = "Server";
    private static final String PROP_ALLOW_MODEL_SCALING = "AllowModelScaling";
    private static final String PROP_ALLOW_MODEL_SCALING_DESC = "Does the server allow scaling of the player model more than the normal size?";
    private static final String PROP_ALLOW_BENDS_PACKS = "AllowBendsPacks";
    private static final String PROP_ALLOW_BENDS_PACKS_DESC = "Does the server allow the use of bends packs?";
    private static final String PROP_MOVEMENT_LIMITED = "MovementLimited";
    private static final String PROP_MOVEMENT_LIMITED_DESC = "Does the server limit excessive bends pack transformation?";

    public CoreServerConfig(File file)
    {
        super(file);
    }

    @Override
    public void save()
    {
        // No config to save.
    }

    private Property getAllowModelScalingProp()
    {
        return configuration.get(CATEGORY_SERVER, PROP_ALLOW_MODEL_SCALING, false, PROP_ALLOW_MODEL_SCALING_DESC);
    }

    private Property getAllowBendsPacksProp()
    {
        return configuration.get(CATEGORY_SERVER, PROP_ALLOW_BENDS_PACKS, false, PROP_ALLOW_BENDS_PACKS_DESC);
    }

    private Property getMovementLimitedProp()
    {
        return configuration.get(CATEGORY_SERVER, PROP_MOVEMENT_LIMITED, true, PROP_MOVEMENT_LIMITED_DESC);
    }

    public void createDefaultConfig()
    {
        getAllowModelScalingProp();
        getAllowBendsPacksProp();
        getMovementLimitedProp();
        configuration.save();
    }

    public boolean isModelScalingAllowed()
    {
        return getAllowModelScalingProp().getBoolean();
    }

    public boolean areBendsPacksAllowed()
    {
        return getAllowBendsPacksProp().getBoolean();
    }

    public boolean isMovementLimited()
    {
        return getMovementLimitedProp().getBoolean();
    }

}
