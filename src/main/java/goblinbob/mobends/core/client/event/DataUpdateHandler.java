package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.data.EntityDatabase;
import net.minecraft.client.Minecraft;

/**
 * The animation's clock: the partial tick, the ticks and the frame the animators run in. Driven by
 * the start of every client tick ({@link #onClientTick}) and of every rendered frame
 * ({@link #onRenderFrame}), see {@code MinecraftMixin} and {@code GameRendererMixin}.
 */
public final class DataUpdateHandler {
    public static float partialTicks = 0.0f;
    private static float ticks = 0.0f;
    public static float ticksPerFrame = 0.0f;
    /**
     * Counts the frames: an entity drawn more than once in one (the player in the world and in the
     * inventory) is animated only the first time.
     */
    public static int frame = 0;

    private DataUpdateHandler() {
    }

    public static float getTicks() {
        return ticks;
    }

    /** At the start of a rendered frame, with the partial tick the entities are drawn at. */
    public static void onRenderFrame(float renderPartialTicks) {
        ++frame;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        // Paused, time stands still: the animators neither advance nor see the partial tick move.
        if (minecraft.isPaused()) {
            ticksPerFrame = 0F;
            return;
        }
        partialTicks = renderPartialTicks;
        float newTicks = minecraft.player.tickCount + renderPartialTicks;
        ticksPerFrame = Math.min(Math.max(0F, newTicks - ticks), 1F);
        ticks = newTicks;
        EntityDatabase.instance.updateRender(renderPartialTicks);
    }

    /** At the start of a client tick, before the entities move. */
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        // Out of a world every entity's data is dropped; paused, the entities don't move.
        if (minecraft.player != null && minecraft.isPaused()) {
            return;
        }
        EntityDatabase.instance.updateClient();
    }
}
