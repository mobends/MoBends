package net.gobbob.mobends.core.client.event;

import net.gobbob.mobends.core.flux.ComputedDependencyHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FluxHandler
{

    @SubscribeEvent
    public void checkDirty(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        ComputedDependencyHelper.reevaluateDirty();
    }

}
