package goblinbob.mobends.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Clips drawing to rectangles of the UI. Clips nest: each one pushed is cut down to the ones
 * already in place, so a thumbnail inside a scrolled list stays inside the list too.
 */
public class UIScissorHelper
{

    public static final UIScissorHelper INSTANCE = new UIScissorHelper();

    /** The clips in place, in pixels ({x, y, width, height}, y from the bottom), innermost first. */
    private final Deque<int[]> clips = new ArrayDeque<>();

    /** Clips drawing to the UI rectangle (within the clips already in place) until {@link #pop()}. */
    public void push(float uiX, float uiY, float uiWidth, float uiHeight)
    {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final double scaleX = (double) mc.displayWidth / scaledResolution.getScaledWidth();
        final double scaleY = (double) mc.displayHeight / scaledResolution.getScaledHeight();

        int left = (int) Math.round(uiX * scaleX);
        int right = (int) Math.round((uiX + uiWidth) * scaleX);
        int bottom = (int) Math.round((scaledResolution.getScaledHeight() - uiY - uiHeight) * scaleY);
        int top = (int) Math.round((scaledResolution.getScaledHeight() - uiY) * scaleY);

        final int[] outer = clips.peek();
        if (outer != null)
        {
            left = Math.max(left, outer[0]);
            bottom = Math.max(bottom, outer[1]);
            right = Math.min(right, outer[0] + outer[2]);
            top = Math.min(top, outer[1] + outer[3]);
        }

        final int[] clip = { left, bottom, Math.max(0, right - left), Math.max(0, top - bottom) };
        clips.push(clip);
        apply(clip);
    }

    /** Takes away the last clip pushed, putting back the one before it (or none). */
    public void pop()
    {
        clips.pop();
        final int[] outer = clips.peek();
        if (outer != null)
        {
            apply(outer);
        }
        else
        {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    private static void apply(int[] clip)
    {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(clip[0], clip[1], clip[2], clip[3]);
    }

}
