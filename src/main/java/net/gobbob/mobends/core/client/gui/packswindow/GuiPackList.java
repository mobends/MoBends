package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiList;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiPackList extends GuiList<GuiPackEntry>
{

    public static final int WIDTH = 126;
    public static final int HEIGHT = 131;

    public GuiPackList()
    {
        super(0, 0, WIDTH, HEIGHT, 3, 5, 5, 5);
    }

    @Override
    public void initGui(int x, int y)
    {
        super.initGui(x, y);
    }

    @Override
    protected void drawBackground()
    {
        super.drawBackground();

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
    }

}
