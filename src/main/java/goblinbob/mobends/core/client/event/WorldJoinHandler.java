package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.AnimationPolicy;
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
            // The entities of the new world reuse the ids of the old one's.
            EntityBenderRegistry.instance.clearCache();

            // The defaults until the server's configuration arrives.
            AnimationPolicy.INSTANCE.onWorldJoin();

            // Sending a request to the server for the server-specific config.
            Core.getNetworkWrapper().sendToServer(new MessageConfigRequest());
        }
    }

}
