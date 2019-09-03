package net.gobbob.mobends.core.client.gui.elements;

import java.util.Iterator;
import java.util.LinkedList;

public class GuiList<T extends IGuiListElement> extends GuiScrollPanel
{

    protected int spacing;
    protected int paddingLeft;
    protected int paddingTop;
    protected int paddingBottom;
    protected LinkedList<T> elements;

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

    protected boolean handleMouseClickedElements(int mouseX, int mouseY, int button)
    {
        Iterator<T> it = this.elements.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseClicked(mouseX, mouseY, button))
                return true;
        }

        return false;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);

        for (T element : this.elements)
        {
            element.update(mouseX - x, mouseY - y + scrollAmount);
        }
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

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        if (this.hovered && handleMouseClickedElements(mouseX - x, mouseY - y + scrollAmount, button))
        {
            return true;
        }

        return super.handleMouseClicked(mouseX, mouseY, button);
    }

}
