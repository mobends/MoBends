package net.gobbob.mobends.core.client.gui.settingswindow;

import net.gobbob.mobends.core.bender.EntityBender;
import net.gobbob.mobends.core.bender.EntityBenderRegistry;
import net.gobbob.mobends.core.client.gui.elements.GuiToggleButton;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

public class GuiBenderSettings
{

    private final EntityBender<?> bender;
    private final Minecraft mc;
    private final GuiToggleButton toggleButton;

    private int x, y;

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

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(toggleButton.getToggleState());
        }
    }

    public void update(int mouseX, int mouseY)
    {
        toggleButton.update(mouseX, mouseY);
    }

    public void draw()
    {
        GlStateManager.color(1F, 1F, 1F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSettingsWindow.BACKGROUND_TEXTURE);

        // Container
        Draw.borderBox(x + 4, y + 4, 100, 30, 4, 36, 126);

        mc.fontRenderer.drawStringWithShadow(bender.getLocalizedName(), this.x + 6, this.y + 4, 0xffffff);

        toggleButton.draw();
    }

}
