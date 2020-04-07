package goblinbob.mobends.core.network;

import goblinbob.mobends.core.network.msg.MessageConfigResponse;
import net.minecraft.client.Minecraft;

/**
 * These are options that are provided by a server the player's playing on.
 * They are default when playing on single-player.
 */
public class NetworkConfiguration
{

	public static NetworkConfiguration instance = new NetworkConfiguration();

	private SharedConfig<Boolean> props = new SharedBooleanProp("prop", false);
	private boolean modelScalingAllowed = false;
    private boolean bendsPacksAllowed = true;
    private boolean movementLimited = true;

    /**
     * Sets up the default permissions before receiving the server's config.
     */
	public void onWorldJoin()
    {
        this.modelScalingAllowed = Minecraft.getMinecraft().isSingleplayer();
        this.bendsPacksAllowed = true;
        this.movementLimited = !Minecraft.getMinecraft().isSingleplayer();
    }

    public void provideServerConfig(MessageConfigResponse message)
    {
        this.modelScalingAllowed = message.allowModelScaling;
        this.bendsPacksAllowed = message.allowBendsPacks;
        this.movementLimited = message.movementLimited;
    }

	public boolean isModelScalingAllowed()
	{
		return modelScalingAllowed;
	}

	public boolean areBendsPacksAllowed()
    {
	    return bendsPacksAllowed;
    }

    public boolean isMovementLimited()
    {
        return movementLimited;
    }

}
