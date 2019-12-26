package net.gobbob.mobends.core.client.gui.settingswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiList;
import net.gobbob.mobends.core.client.gui.packswindow.GuiPacksWindow;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;

public class GuiBenderList extends GuiList<GuiBenderSettings>
{

    private final LinkedList<GuiBenderSettings> elements;

    public GuiBenderList(int x, int y, int width, int height)
    {
        super(x, y, width, height, 10, 3, 3, 15);
        this.elements = new LinkedList<>();
    }

    @Override
    protected void drawBackground(float partialTicks)
    {
        super.drawBackground(partialTicks);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
    }

    @Override
    public LinkedList<GuiBenderSettings> getListElements()
    {
        return elements;
    }

    @Override
    protected int getScrollSpeed()
    {
        return 20;
    }

}
