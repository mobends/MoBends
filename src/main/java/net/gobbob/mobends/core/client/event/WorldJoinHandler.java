package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.Core;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldJoinHandler
{

    @SubscribeEvent
    public void onPlayerJoinedServer(EntityJoinWorldEvent event)
    {
        Core.LOG.info("Joined the server!");
    }

}
