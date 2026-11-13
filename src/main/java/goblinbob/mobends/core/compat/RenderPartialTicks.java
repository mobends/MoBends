package goblinbob.mobends.core.compat;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * The partial ticks an entity is drawn with, which 1.8.9's {@code RenderLivingEvent} doesn't carry (later versions'
 * does). The world draws its entities with the frame's partial ticks; a screen (the inventory) draws them with 1.0,
 * as vanilla's {@code GuiInventory#drawEntityOnScreen} does; whoever draws an entity with other partial ticks says so
 * with {@link #drawingWith}.
 */
public final class RenderPartialTicks
{

    private static float frame;
    private static boolean inScreen;
    /** Set while {@link #drawingWith} draws, NaN otherwise. */
    private static float override = Float.NaN;

    private RenderPartialTicks()
    {
    }

    /** The partial ticks of the entity being drawn. */
    public static float get()
    {
        if (!Float.isNaN(override)) return override;
        return inScreen ? 1.0F : frame;
    }

    /** Runs {@code draw}, which draws entities with {@code partialTicks}. */
    public static void drawingWith(float partialTicks, Runnable draw)
    {
        final float previous = override;
        override = partialTicks;
        try
        {
            draw.run();
        }
        finally
        {
            override = previous;
        }
    }

    /** Listens to the frames and the screens (see {@link Events#register}). */
    public static final class Tracker
    {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onRenderTick(TickEvent.RenderTickEvent event)
        {
            if (event.phase == TickEvent.Phase.START)
            {
                frame = event.renderTickTime;
                inScreen = false;
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event)
        {
            inScreen = true;
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event)
        {
            inScreen = false;
        }
    }
}
