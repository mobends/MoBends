package net.gobbob.mobends.core.client.gui.settingswindow;

import net.gobbob.mobends.core.bender.EntityBender;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.client.gui.elements.IGuiListElement;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiBenderSettings implements IGuiListElement
{

    private final EntityBender<?> bender;
    private final Minecraft mc;
    private final GuiToggleButton toggleButton;

    private int x, y;
    private int listOrder;

    public GuiBenderSettings(EntityBender<?> bender)
    {
        this.bender = bender;
        this.mc = Minecraft.getMinecraft();
        this.toggleButton = new GuiToggleButton("Animated", 32);
        this.toggleButton.setToggleState(bender.isAnimated());
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.toggleButton.initGui(x + 4, y + 14);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(toggleButton.getToggleState());
            return true;
        }

        return false;
    }

    public void update(int mouseX, int mouseY)
    {
        toggleButton.update(mouseX, mouseY);
    }

    public void draw(float partialTicks)
    {
        GlStateManager.color(1F, 1F, 1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSettingsWindow.BACKGROUND_TEXTURE);

        // Container
        Draw.borderBox(x + 4, y + 4, 100, 30, 4, 36, 126);

        mc.fontRenderer.drawStringWithShadow(bender.getLocalizedName(), this.x + 6, this.y + 4, 0xffffff);

        toggleButton.draw();
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getHeight()
    {
        return 30;
    }

    @Override
    public int getOrder()
    {
        return listOrder;
    }

    @Override
    public void setOrder(int order)
    {
        listOrder = order;
    }

}
