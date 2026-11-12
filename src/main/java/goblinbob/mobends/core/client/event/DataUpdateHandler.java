package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.kumo.RayGizmos;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DataUpdateHandler
{

    public static float partialTicks = 0.0f;
    protected static float ticks = 0.0f;
    public static float ticksPerFrame = 0.0f;
    /**
     * Counts the frames: an entity drawn more than once in one (the player in the world and in the
     * inventory) is animated only the first time.
     */
    public static int frame = 0;
    /**
     * Client ticks out of a world, the clock of the Mo' Bends menu opened from Forge's Mods list:
     * its buttons' animations and the previews of its windows.
     */
    private static int ticksOutOfWorld = 0;

    public static float getTicks()
    {
        return ticks;
    }

    @SubscribeEvent
    public void updateAnimations(TickEvent.RenderTickEvent event)
    {
        if (event.phase == Phase.END)
            return;
        ++DataUpdateHandler.frame;
        if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null)
        {
            updateOutOfWorld(event.renderTickTime);
            return;
        }

        if (!Minecraft.getMinecraft().isGamePaused())
        {
            DataUpdateHandler.partialTicks = event.renderTickTime;
        }

        final float newTicks = Minecraft.getMinecraft().thePlayer.ticksExisted + event.renderTickTime;

        if (!(Minecraft.getMinecraft().theWorld.isRemote && Minecraft.getMinecraft().isGamePaused()))
        {
            DataUpdateHandler.ticksPerFrame = Math.min(Math.max(0F, newTicks - DataUpdateHandler.ticks), 1F);
            DataUpdateHandler.ticks = newTicks;

            RayGizmos.beginFrame();
            EntityDatabase.instance.updateRender(event.renderTickTime);
            Addons.onRenderTick(event.renderTickTime);
        }
        else
        {
            DataUpdateHandler.ticksPerFrame = 0F;
        }
    }

    /** Out of a world, time goes on for the menus: only the previews are in the entity database. */
    private static void updateOutOfWorld(float renderTickTime)
    {
        DataUpdateHandler.partialTicks = renderTickTime;
        final float newTicks = ticksOutOfWorld + renderTickTime;
        DataUpdateHandler.ticksPerFrame = Math.min(Math.max(0F, newTicks - DataUpdateHandler.ticks), 1F);
        DataUpdateHandler.ticks = newTicks;
        EntityDatabase.instance.updateRender(renderTickTime);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == Phase.END)
            return;
        if (Minecraft.getMinecraft().thePlayer == null)
        {
            ++ticksOutOfWorld;
            EntityDatabase.instance.updateClient();
            return;
        }
        if (Minecraft.getMinecraft().isGamePaused())
        {
            EntityDatabase.instance.tickPaused();
            return;
        }

        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

}
