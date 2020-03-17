package goblinbob.mobends.core.network;

import goblinbob.mobends.core.network.msg.MessageClientConfigure;
import net.minecraft.client.Minecraft;

/**
 * These are options that are provided by a server the player's playing on.
 * They are default when playing on single-player.
 */
public class NetworkConfiguration
{

	public static NetworkConfiguration instance = new NetworkConfiguration();

	private boolean allowModelScaling = false;

	public void onWorldJoin()
    {
        this.allowModelScaling = false;
    }

    public void provideServerConfig(MessageClientConfigure message)
    {
        this.allowModelScaling = message.allowModelScaling;
    }

	public boolean isModelScalingAllowed()
	{
		return allowModelScaling || Minecraft.getMinecraft().isSingleplayer();
	}

}
