package goblinbob.mobends.core.network;

import net.minecraft.client.Minecraft;

/**
 * These are options that are provided by a server the player's playing on.
 * They are default when playing on single-player.
 */
public class NetworkConfiguration
{

	public static NetworkConfiguration instance = new NetworkConfiguration();

	private final SharedConfig sharedConfig = new SharedConfig();
	private final SharedProperty<Boolean> modelScalingAllowed;

    public NetworkConfiguration()
    {
        sharedConfig.addProperty(modelScalingAllowed = new SharedBooleanProp(
                "modelScalingAllowed",
                false,
                "Does the server allow scaling of the player model more than the normal size?"));
    }

    /**
     * Sets up the default permissions before receiving the server's config.
     */
	public void onWorldJoin()
    {
        this.modelScalingAllowed.setValue(Minecraft.getMinecraft().isSingleplayer());
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

	public boolean isModelScalingAllowed()
	{
		return modelScalingAllowed.getValue();
	}

}
