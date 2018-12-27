package net.gobbob.mobends.core.event;

import net.gobbob.mobends.core.main.MoBends;
import net.gobbob.mobends.core.network.NetworkConfiguration;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer {
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if(event.getEntity() == null || !(event.getEntity() instanceof EntityPlayerMP)) return;
		
		MoBends.instance.networkWrapper.sendTo(new MessageClientConfigure(NetworkConfiguration.instance.allowModelScaling), (EntityPlayerMP) event.getEntity());
	}
}
