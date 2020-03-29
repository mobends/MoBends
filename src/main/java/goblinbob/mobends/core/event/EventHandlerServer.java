package goblinbob.mobends.core.event;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreServer;
import goblinbob.mobends.core.network.msg.MessageClientConfigure;
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

        if (CoreServer.getInstance() != null)
        {
            CoreServer core = CoreServer.getInstance();
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
