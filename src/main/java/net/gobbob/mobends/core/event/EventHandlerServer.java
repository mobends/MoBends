package net.gobbob.mobends.core.event;

import net.gobbob.mobends.core.Core;
import net.gobbob.mobends.core.CoreServer;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer
{

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if (event.getEntity() == null || !(event.getEntity() instanceof EntityPlayerMP))
        {
            return;
        }

        if (Core.INSTANCE instanceof CoreServer)
        {
            CoreServer core = (CoreServer) Core.INSTANCE;
            Core.getNetworkWrapper().sendTo(
                    new MessageClientConfigure(core.getConfiguration().isModelScalingAllowed()),
                    (EntityPlayerMP) event.getEntity());
        }
        else
        {
            Core.LOG.severe("The CORE isn't a server core, something is wrong...");
        }
    }

}
