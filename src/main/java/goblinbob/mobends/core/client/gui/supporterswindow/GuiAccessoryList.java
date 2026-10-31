package goblinbob.mobends.core.client.gui.supporterswindow;

import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

import java.util.LinkedList;

/** The accessories of the Supporters window, drawn like the Settings window's list of mobs. */
public class GuiAccessoryList extends GuiList<GuiAccessoryRow>
{

    private final LinkedList<GuiAccessoryRow> elements = new LinkedList<>();

    public GuiAccessoryList(int x, int y, int width, int height)
    {
        super(x, y, width, height, 3, 3, 3, 3);
    }

    /** How wide a row is: the list but its padding and the scroll bar. */
    public int getRowWidth()
    {
        return this.width - this.scrollBarWidth - 2 * this.paddingLeft;
    }

    @Override
    protected void drawBackground(float partialTicks)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
        Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
    }

    @Override
    protected void drawContent(float partialTicks)
    {
        // Rows scrolled out of view aren't drawn.
        final float scroll = getScrollAmount(partialTicks);
        for (GuiAccessoryRow element : elements)
        {
            if (element.getY() + element.getHeight() > scroll && element.getY() < scroll + this.height)
            {
                element.draw(partialTicks);
            }
        }
    }

    @Override
    public LinkedList<GuiAccessoryRow> getListElements()
    {
        return elements;
    }

    @Override
    protected int getScrollSpeed()
    {
        return 20;
    }

}
