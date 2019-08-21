package net.gobbob.mobends.core.client.gui.elements;

public interface IGuiListElement
{

    void initGui(int x, int y);

    int getX();

    int getY();

    int getHeight();

    void draw();

}
