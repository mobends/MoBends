package net.gobbob.mobends.core.client.gui.elements;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class GuiList<T extends IGuiListElement> extends GuiScrollPanel
{

    protected int spacing;
    protected int paddingLeft;
    protected int paddingTop;
    protected int paddingBottom;

    public GuiList(int x, int y, int width, int height, int spacing, int paddingLeft, int paddingTop, int paddingBottom)
    {
        super(null, x, y, width, height);
        this.spacing = spacing;
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    public GuiList(int x, int y, int width, int height)
    {
        this(x, y, width, height, 10, 0, 0, 0);
    }

    public abstract LinkedList<T> getListElements();

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        int yOffset = paddingTop;
        for (T entry : this.getListElements())
        {
            entry.initGui(paddingLeft, yOffset);
            yOffset += entry.getHeight() + spacing;
        }
        yOffset -= spacing;

        this.contentSize = yOffset + paddingBottom;

        if (contentSize <= height)
        {
            this.scrollAmountTarget = 0;
        }
        else if (this.scrollAmountTarget + this.height >  contentSize)
        {
            this.scrollAmountTarget = contentSize - this.height;
            this.scrollAmount = contentSize - this.height;
        }
    }

    public void addElement(T element)
    {
        List<T> elements = getListElements();
        elements.add(element);
        element.setOrder(elements.size() - 1);
        this.initGui(this.x, this.y);
    }

    /**
     * Changes an element's order in the list, or inserts it at that index.
     * @param element
     * @param newIndex
     */
    public void insertOrMoveElement(T element, int newIndex)
    {
        final List<T> elements = getListElements();

        if (elements.contains(element))
        {
            elements.remove(element);
        }

        try
        {
            elements.add(newIndex, element);
            element.setOrder(newIndex);
        }
        catch(IndexOutOfBoundsException ex)
        {
            elements.add(element);
        }

        this.initGui(this.x, this.y);
    }

    public void removeElement(T element)
    {
        this.getListElements().remove(element);

        this.initGui(this.x, this.y);
    }

    protected boolean handleMouseClickedElements(int mouseX, int mouseY, int button)
    {
        Iterator<T> it = this.getListElements().descendingIterator();
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

        for (T element : this.getListElements())
        {
            element.update(mouseX - x, mouseY - y + scrollAmount);
        }
    }

    @Override
    protected void drawContent()
    {
        for (T element : this.getListElements())
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

    public int getSpacing()
    {
        return spacing;
    }

    public int getPaddingTop()
    {
        return paddingTop;
    }

    public int getPaddingLeft()
    {
        return paddingLeft;
    }

    public int getPaddingBottom()
    {
        return paddingBottom;
    }

}
