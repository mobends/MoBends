package net.gobbob.mobends.core.client.gui.elements;

public interface IGuiListElement
{

    void initGui(int x, int y);

    boolean handleMouseClicked(int mouseX, int mouseY, int state);

    void update(int mouseX, int mouseY);

    void draw(float partialTicks);

    int getX();

    int getY();

    int getHeight();

    int getOrder();

    void setOrder(int order);

}
