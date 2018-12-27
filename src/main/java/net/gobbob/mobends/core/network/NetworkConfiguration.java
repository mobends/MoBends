package net.gobbob.mobends.core.network;

import net.minecraft.client.Minecraft;

public class NetworkConfiguration
{
	public static NetworkConfiguration instance = new NetworkConfiguration();
	public boolean allowModelScaling = false;

	public boolean isModelScalingAllowed()
	{
		return allowModelScaling || Minecraft.getMinecraft().isSingleplayer();
	}
}
