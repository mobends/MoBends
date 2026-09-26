package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.msg.MessageConfigRequest;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldJoinHandler
{

    @SubscribeEvent
    public void onPlayerJoinedServer(EntityJoinWorldEvent event)
    {
        // Only the local player joining means joining a world; other players join it all the time.
        if (event.getEntity() instanceof EntityPlayerSP)
        {
            // The defaults until the server's configuration arrives.
            NetworkConfiguration.instance.onWorldJoin();

            // Sending a request to the server for the server-specific config.
            Core.getNetworkWrapper().sendToServer(new MessageConfigRequest());
        }
    }

}
