package net.gobbob.mobends.network;

import net.minecraft.client.Minecraft;

public class NetworkConfiguration {
	public static boolean allowModelScaling = false;
	
	public static boolean isModelScalingAllowed() {
		return allowModelScaling || Minecraft.getMinecraft().isSingleplayer();
	}
}
