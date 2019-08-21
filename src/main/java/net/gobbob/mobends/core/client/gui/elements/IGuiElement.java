package net.gobbob.mobends.core.client.gui.elements;

public interface IGuiElement extends IGuiPositioned
{

    /**
     * This method gets run every frame. It passes mouse coordinates relative to the parent.
     * @param mouseX x coordinate relative to the parent
     * @param mouseY y coordinate relative to the parent
     */
    void update(int mouseX, int mouseY);

    /**
     * This method gets called on mouse click. It passes mouse coordinates relative to the parent.
     * @param mouseX x coordinate relative to the parent
     * @param mouseY y coordinate relative to the parent
     * @param button The button clicked
     */
    boolean handleMouseClicked(int mouseX, int mouseY, int button);

    void initGui();
    void draw();
    IGuiElement getParent();

    /**
     * @return the value of x used to position child elements.
     */
    default float getViewX() { return getX(); }

    /**
     * @return the value of y used to position child elements.
     */
    default float getViewY() { return getY(); }

    default float getAbsoluteX()
    {
        IGuiElement parent = getParent();
        if (parent != null)
            return parent.getAbsoluteX() + this.getViewX();
        else
            return this.getX();
    }

    default float getAbsoluteY()
    {
        IGuiElement parent = getParent();
        if (parent != null)
            return parent.getAbsoluteY() + this.getViewY();
        else
            return this.getY();
    }

}
