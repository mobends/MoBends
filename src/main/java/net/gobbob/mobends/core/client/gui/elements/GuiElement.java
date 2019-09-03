package net.gobbob.mobends.core.client.gui.elements;

import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;

public abstract class GuiElement implements IGuiElement, IGuiElementsContainer
{

    protected int x;
    protected int y;

    protected IGuiElement parent;
    protected LinkedList<IGuiElement> children;

    public GuiElement(IGuiElement parent, int x, int y)
    {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.children = new LinkedList<>();
    }

    @Override
    public void initGui()
    {
        this.children.clear();
    }

    @Override
    public IGuiElement getParent() {
        return this.parent;
    }

    @Override
    public LinkedList<IGuiElement> getElements() {
        return this.children;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.updateChildren(mouseX, mouseY);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        return this.handleMouseClickedChildren(mouseX, mouseY, button);
    }

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        return this.handleMouseReleasedChildren(mouseX, mouseY, button);
    }

    @Override
    public void draw()
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground();
        this.drawChildren();
        this.drawForeground();

        GlStateManager.popMatrix();
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    protected abstract void drawBackground();

    protected abstract void drawForeground();

}
