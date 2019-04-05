package net.gobbob.mobends.core.client.gui.elements;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiToggleButton
{

    public static final int WIDTH = 20;
    public static final int HEIGHT = 16;

    protected int x, y;
    protected boolean hovered;

    public GuiToggleButton()
    {
        this.x = 0;
        this.y = 0;
        this.hovered = false;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    public void display()
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        int textureY = hovered ? 64 : 44;
        Draw.texturedModalRect(x, y, 88, textureY, WIDTH, HEIGHT);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int state)
    {
        return hovered;
    }

}
