package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiList;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

import java.util.Iterator;

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

    @Override
    protected boolean handleMouseClickedElements(int mouseX, int mouseY, int button)
    {
        Iterator<GuiPackEntry> it = this.elements.descendingIterator();
        while (it.hasNext())
        {
            GuiPackEntry clickedEntry = it.next();
            if (clickedEntry.handleMouseClicked(mouseX, mouseY, button))
            {
                this.elements.forEach(entry -> entry.setSelected(entry == clickedEntry));
                return true;
            }
        }

        return false;
    }

}
