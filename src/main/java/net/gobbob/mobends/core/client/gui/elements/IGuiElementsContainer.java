package net.gobbob.mobends.core.client.gui.elements;

import java.util.Collection;

public interface IGuiElementsContainer extends IGuiPositioned
{

    Collection<IGuiElement> getElements();

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

}
