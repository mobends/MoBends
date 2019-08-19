package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.addon.Addons;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DataUpdateHandler
{
	public static int lastNotedClientTick = 0;
	public static float partialTicks = 0.0f;
	protected static float ticks = 0.0f;
	public static float ticksPerFrame = 0.0f;

	public static float getTicks()
	{
		return ticks;
	}

	@SubscribeEvent
	public void updateAnimations(TickEvent.RenderTickEvent event)
	{
		if (event.phase == Phase.END)
			return;
		if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
			return;

		partialTicks = event.renderTickTime;

		float totalTicks = Minecraft.getMinecraft().player.ticksExisted + event.renderTickTime;

		if (ticks > totalTicks)
		{
			onTicksRestart();
		}

		ticksPerFrame = Math.min(Math.max(0F, totalTicks - ticks), 1.0F);
		ticks = totalTicks;
		
		if (!(Minecraft.getMinecraft().world.isRemote && Minecraft.getMinecraft().isGamePaused()))
		{
			EntityDatabase.instance.updateRender(event.renderTickTime);
			Addons.onRenderTick(event.renderTickTime);
		}
	}

	public static void onTicksRestart()
	{
		EntityDatabase.instance.onTicksRestart();
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == Phase.END || Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().isGamePaused())
			return;
		
		lastNotedClientTick = Minecraft.getMinecraft().player.ticksExisted;
		EntityDatabase.instance.updateClient();
		Addons.onClientTick();
	}

}
