package net.gobbob.mobends.core.event;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.network.NetworkConfiguration;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.gobbob.mobends.standard.main.MoBends;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer
{
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() == null || !(event.getEntity() instanceof EntityPlayerMP))
			return;

		Core.getNetworkWrapper().sendTo(
				new MessageClientConfigure(NetworkConfiguration.instance.allowModelScaling),
				(EntityPlayerMP) event.getEntity());
	}
}
