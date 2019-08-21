package net.gobbob.mobends.core.client.gui.elements;

import java.util.LinkedList;
import java.util.List;

public class GuiList<T extends IGuiListElement> extends GuiScrollPanel
{

    protected int spacing;
    protected int paddingLeft;
    protected int paddingTop;
    protected int paddingBottom;
    protected List<T> elements;

    public GuiList(int x, int y, int width, int height, int spacing, int paddingLeft, int paddingTop, int paddingBottom)
    {
        super(null, x, y, width, height);
        this.spacing = spacing;
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.elements = new LinkedList<>();
    }

    public GuiList(int x, int y, int width, int height)
    {
        this(x, y, width, height, 10, 0, 0, 0);
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        int yOffset = paddingTop;
        for (T entry : this.elements)
        {
            entry.initGui(paddingLeft, yOffset);
            yOffset += entry.getHeight() + spacing;
        }
        yOffset -= spacing;

        this.contentSize = yOffset + paddingBottom;
    }

    public void addElement(T element)
    {
        this.elements.add(element);
        this.initGui(this.x, this.y);
    }

    @Override
    protected void drawContent()
    {
        for (T element : this.elements)
        {
            element.draw();
        }
    }

    @Override
    protected void drawBackground()
    {

    }

}
