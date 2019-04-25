package net.gobbob.mobends.core.client.gui.elements;

import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiElement
{

    protected int x;
    protected int y;

    protected GuiElement parent;
    protected List<GuiElement> children;

    public GuiElement(GuiElement parent, int x, int y)
    {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.children = new ArrayList<>();
    }

    public void addElement(GuiElement element)
    {
        this.children.add(element);
        element.initGui();
    }

    public void initGui()
    {
        this.children.clear();
    }

    /**
     * This method gets run every frame. It passes mouse coordinates relative to the parent.
     * @param mouseX x coordinate relative to the parent
     * @param mouseY y coordinate relative to the parent
     */
    public void update(int mouseX, int mouseY)
    {
        for (int i = this.children.size() - 1; i >= 0; --i)
        {
            this.children.get(i).update(mouseX - x, mouseY - y);
        }
    }

    public void draw()
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground();
        this.drawElements();
        this.drawForeground();

        GlStateManager.popMatrix();
    }

    protected void drawElements()
    {
        for (GuiElement element : children)
        {
            element.draw();
        }
    }

    protected abstract void drawBackground();

    protected abstract void drawForeground();

    /**
     * @return the direct value of the x variable.
     */
    public int getX()
    {
        return x;
    }

    /**
     * @return the direct value of the y variable.
     */
    public int getY()
    {
        return y;
    }

    /**
     * @return the value of x used to position child elements.
     */
    public float getViewX() { return x; }

    /**
     * @return the value of y used to position child elements.
     */
    public float getViewY() { return y; }

    public float getAbsoluteX()
    {
        if (this.parent != null)
            return this.parent.getAbsoluteX() + this.getViewX();
        else
            return this.x;
    }

    public float getAbsoluteY()
    {
        if (this.parent != null)
            return this.parent.getAbsoluteY() + this.getViewY();
        else
            return this.y;
    }

}
