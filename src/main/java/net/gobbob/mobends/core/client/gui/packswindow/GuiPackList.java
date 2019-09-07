package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiList;
import net.gobbob.mobends.core.flux.Observable;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

import java.util.Iterator;
import java.util.LinkedList;

public class GuiPackList extends GuiList<GuiPackEntry>
{

    public static final int WIDTH = 126;
    public static final int HEIGHT = 131;

    private LinkedList<GuiPackEntry> listEntries;
    public final Observable<GuiPackEntry> elementClickedObservable;

    public GuiPackList(LinkedList<GuiPackEntry> listEntries)
    {
        super(0, 0, WIDTH, HEIGHT, 3, 5, 5, 5);
        this.listEntries = listEntries;
        this.elementClickedObservable = new Observable<>();
    }

    @Override
    public LinkedList<GuiPackEntry> getListElements()
    {
        return listEntries;
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
    protected void drawContent()
    {
        for (GuiPackEntry element : this.getListElements())
        {
            // Dragged elements are drawn separately.
            if (!element.isDragged())
            {
                element.draw();
            }
        }
    }

    @Override
    protected boolean handleMouseClickedElements(int mouseX, int mouseY, int button)
    {
        Iterator<GuiPackEntry> it = getListElements().descendingIterator();
        while (it.hasNext())
        {
            GuiPackEntry clickedEntry = it.next();
            if (clickedEntry.handleMouseClicked(mouseX, mouseY, button))
            {
                getListElements().forEach(entry -> entry.setSelected(entry == clickedEntry));
                elementClickedObservable.next(clickedEntry);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        return super.handleMouseReleased(mouseX, mouseY, button);
    }

}
