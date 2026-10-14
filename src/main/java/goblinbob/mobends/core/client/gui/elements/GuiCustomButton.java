package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiCustomButton extends GuiButton
{

    private final Minecraft mc;

    public GuiCustomButton(int buttonId, int width, int height)
    {
        super(buttonId, 0, 0, width, height, "");
        mc = Minecraft.getMinecraft();
    }

    public GuiCustomButton(int width, int height, String text)
    {
        super(0, 0, 0, width, height, text);
        mc = Minecraft.getMinecraft();
    }

    public GuiCustomButton setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    public void drawButton(int mouseX, int mouseY, float partialTicks)
    {
        this.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    /** As vanilla's, but a button less than 20 high keeps its bottom edge: its top and bottom halves are drawn apart. */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.height >= 20)
        {
            super.drawButton(mc, mouseX, mouseY, partialTicks);
            return;
        }
        if (!this.visible)
        {
            return;
        }

        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        final int textureY = 46 + this.getHoverState(this.hovered) * 20;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        final int left = this.width / 2;
        final int right = this.width - left;
        final int top = this.height / 2;
        final int bottom = this.height - top;
        this.drawTexturedModalRect(this.x, this.y, 0, textureY, left, top);
        this.drawTexturedModalRect(this.x + left, this.y, 200 - right, textureY, right, top);
        this.drawTexturedModalRect(this.x, this.y + top, 0, textureY + 20 - bottom, left, bottom);
        this.drawTexturedModalRect(this.x + left, this.y + top, 200 - right, textureY + 20 - bottom, right, bottom);
        this.mouseDragged(mc, mouseX, mouseY);

        int color = 14737632;
        if (this.packedFGColour != 0)
            color = this.packedFGColour;
        else if (!this.enabled)
            color = 10526880;
        else if (this.hovered)
            color = 16777120;
        this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
    }

    public boolean mousePressed(int mouseX, int mouseY)
    {
        final boolean clicked = this.mousePressed(mc, mouseX, mouseY);

        if (clicked)
        {
            this.playPressSound(mc.getSoundHandler());
        }

        return clicked;
    }

    public GuiCustomButton setText(String text)
    {
        this.displayString = text;
        return this;
    }

}
