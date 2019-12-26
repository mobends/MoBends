package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiElement;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiAddPackButton extends GuiElement
{

    private static final int WIDTH = 100;
    private static final int HEIGHT = 12;
    private boolean hover;
    private boolean enabled;

    public GuiAddPackButton(GuiElement parent, int x, int y)
    {
        super(parent, x, y);
        this.x = 0;
        this.y = 0;
        this.hover = false;
        this.enabled = true;
    }

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);

        this.hover = mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    @Override
    protected void drawBackground(float partialTicks)
    {
        if (!this.enabled)
            return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        int textureY = hover ? 105 : 93;
        Draw.texturedModalRect(0, 0, 0, textureY, WIDTH, HEIGHT);
    }

    @Override
    protected void drawForeground(float partialTicks) {}

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (!enabled)
            return false;

        return this.hover = mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    public GuiAddPackButton enable()
    {
        this.enabled = true;
        return this;
    }

    public GuiAddPackButton disable()
    {
        this.enabled = false;
        return this;
    }

}
