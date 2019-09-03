package net.gobbob.mobends.core.client.gui.elements;

public interface IGuiListElement
{

    void initGui(int x, int y);

    int getX();

    int getY();

    int getHeight();

    boolean handleMouseClicked(int mouseX, int mouseY, int state);

    void update(int mouseX, int mouseY);

    void draw();

}
