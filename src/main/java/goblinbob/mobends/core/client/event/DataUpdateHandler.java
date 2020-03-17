package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.data.EntityDatabase;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DataUpdateHandler
{

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

        if (!Minecraft.getMinecraft().isGamePaused())
        {
            DataUpdateHandler.partialTicks = event.renderTickTime;
        }

        final float newTicks = Minecraft.getMinecraft().player.ticksExisted + event.renderTickTime;

        if (DataUpdateHandler.ticks > newTicks)
        {
            onTicksRestart();
        }

        if (!(Minecraft.getMinecraft().world.isRemote && Minecraft.getMinecraft().isGamePaused()))
        {
            DataUpdateHandler.ticksPerFrame = Math.min(Math.max(0F, newTicks - DataUpdateHandler.ticks), 1F);
            DataUpdateHandler.ticks = newTicks;

            EntityDatabase.instance.updateRender(event.renderTickTime);
            Addons.onRenderTick(event.renderTickTime);
        }
        else
        {
            DataUpdateHandler.ticksPerFrame = 0F;
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

        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

}
