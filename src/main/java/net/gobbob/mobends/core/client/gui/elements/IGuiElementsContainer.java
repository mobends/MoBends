package net.gobbob.mobends.core.client.gui.elements;

import java.util.Iterator;
import java.util.LinkedList;

public interface IGuiElementsContainer extends IGuiPositioned
{

    LinkedList<IGuiElement> getElements();

    default void addElement(IGuiElement element)
    {
        this.getElements().add(element);
        element.initGui();
    }

    default void drawChildren()
    {
        for (IGuiElement element : getElements())
        {
            element.draw();
        }
    }

    default void updateChildren(int mouseX, int mouseY)
    {
        for (IGuiElement element : getElements())
        {
            element.update(mouseX - getX(), mouseY - getY());
        }
    }

    default boolean handleMouseClickedChildren(int mouseX, int mouseY, int button)
    {
        Iterator<IGuiElement> it = this.getElements().descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseClicked(mouseX, mouseY, button))
                return true;
        }

        return false;
    }

}
