package net.gobbob.mobends.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class UIScissorHelper
{

    public static final UIScissorHelper INSTANCE = new UIScissorHelper();

    private int x;
    private int y;
    private int width;
    private int height;

    public UIScissorHelper()
    {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public void setUIBounds(int uiX, int uiY, int uiWidth, int uiHeight)
    {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        final int scaledWidth = scaledResolution.getScaledWidth();
        final int scaledHeight = scaledResolution.getScaledHeight();

        this.x = uiX * Minecraft.getMinecraft().displayWidth / scaledWidth;
        this.y = (scaledHeight - uiY - uiHeight) * Minecraft.getMinecraft().displayHeight / scaledHeight;
        this.width = uiWidth * Minecraft.getMinecraft().displayWidth / scaledWidth;
        this.height = uiHeight * Minecraft.getMinecraft().displayHeight / scaledHeight;
    }

    public void enable()
    {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    public void disable()
    {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

}
