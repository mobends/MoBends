package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;

public class GuiBenderList extends GuiList<GuiBenderSettings>
{

    /** The list's frame and rows. */
    public static final ResourceLocation LIST_TEXTURE = new ResourceLocation(ModStatics.MODID, "textures/gui/settings_window.png");


    private final LinkedList<GuiBenderSettings> elements;

    public GuiBenderList(int x, int y, int width, int height)
    {
        super(x, y, width, height, 3, 3, 3, 3);
        this.elements = new LinkedList<>();
    }

    /** How wide a row is: the list but its padding and the scroll bar. */
    public int getRowWidth()
    {
        return this.width - this.scrollBarWidth - 2 * this.paddingLeft;
    }

    @Override
    protected void drawBackground(float partialTicks)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(LIST_TEXTURE);
        Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
    }

    @Override
    protected void drawContent(float partialTicks)
    {
        // Rows scrolled out of view aren't drawn, so their mobs aren't rendered.
        final float scroll = getScrollAmount(partialTicks);
        for (GuiBenderSettings element : elements)
        {
            if (element.getY() + element.getHeight() > scroll && element.getY() < scroll + this.height)
            {
                element.draw(partialTicks);
            }
        }
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
