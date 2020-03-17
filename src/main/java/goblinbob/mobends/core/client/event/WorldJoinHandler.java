package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.Core;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldJoinHandler
{

    @SubscribeEvent
    public void onPlayerJoinedServer(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof AbstractClientPlayer)
        {
            Core.LOG.info("Joined the server!");
        }
    }

}
