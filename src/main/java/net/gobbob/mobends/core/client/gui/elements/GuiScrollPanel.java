package net.gobbob.mobends.core.client.gui.elements;

import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.UIScissorHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

public abstract class GuiScrollPanel extends GuiElement
{

    protected int width;
    protected int height;
    protected int scrollBarWidth;

    protected int contentSize; // width/height of the full content
    protected int scrollAmount;
    protected boolean hovered;
    protected boolean scrollBarHovered;
    protected boolean scrollHandleHovered;
    protected boolean scrollBarGrabbed;
    protected int scrollBarGrabY;

    public GuiScrollPanel(GuiElement parent, int x, int y, int width, int height)
    {
        super(parent, x, y);
        this.width = width;
        this.height = height;
        this.scrollBarWidth = 6;
        this.contentSize = 0;
        this.scrollAmount = 0;
        this.hovered = false;
        this.scrollBarHovered = false;
        this.scrollHandleHovered = false;
        this.scrollBarGrabbed = false;
        this.scrollBarGrabY = 0;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        this.scrollBarHovered = false;
        this.scrollHandleHovered = false;

        if (this.scrollBarGrabbed)
        {
            this.scrollTo((int) (mouseY - y - scrollBarGrabY) * (contentSize) / height );
        }

        if (this.hovered)
        {
            if (mouseX >= this.x + this.width - this.scrollBarWidth)
            {
                this.scrollBarHovered = true;

                final int scrollHandleY = getScrollHandleY();
                final int scrollBarHeight = this.getScrollHandleHeight();
                if (mouseY >= this.y + scrollHandleY && mouseY <= y + scrollHandleY + scrollBarHeight)
                {
                    this.scrollHandleHovered = true;
                }
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int event)
    {
        this.scrollBarGrabbed = false;

        if (scrollBarHovered)
        {
            if (scrollHandleHovered)
            {
                scrollBarGrabY = mouseY - y - getScrollHandleY();
            }
            else
            {
                scrollBarGrabY = getScrollHandleHeight() / 2;
            }
            scrollBarGrabbed = true;

            return true;
        }

        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int event)
    {
        this.scrollBarGrabbed = false;
    }

    public boolean handleMouseInput()
    {
        int mouseWheelRoll = -Mouse.getEventDWheel();
        if (this.hovered)
        {
            if (mouseWheelRoll != 0)
            {
                mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;
                this.scroll(mouseWheelRoll * 10);
            }
            return true;
        }

        return false;
    }

    protected void scrollTo(int value)
    {
        if (contentSize <= height)
        {
            this.scrollAmount = 0;
            return;
        }

        this.scrollAmount = value;

        if (this.scrollAmount < 0)
            this.scrollAmount = 0;
        else if (this.scrollAmount > contentSize - height)
            this.scrollAmount = contentSize - height;
    }

    protected void scroll(int amount)
    {
        scrollTo(this.scrollAmount + amount);
    }

    @Override
    public void draw()
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -this.scrollAmount, 0);
        UIScissorHelper.INSTANCE.setUIBounds((int) this.getAbsoluteX(), (int) this.getAbsoluteY(), this.width - this.scrollBarWidth, this.height);
        UIScissorHelper.INSTANCE.enable();

        this.drawElements();
        this.drawContent();

        UIScissorHelper.INSTANCE.disable();
        GlStateManager.popMatrix();

        this.drawForeground();

        GlStateManager.popMatrix();
    }

    protected abstract void drawContent();

    @Override
    protected void drawForeground()
    {
        this.drawScrollBar();
    }

    protected void drawScrollBar()
    {
        if (contentSize <= height)
            return;

        final int scrollBarHeight = this.getScrollHandleHeight();

        // Background
        Draw.rectangle(width - scrollBarWidth, 0, scrollBarWidth, height, 0xff00406b);

        // Handle
        Draw.rectangle(width - scrollBarWidth, this.getScrollHandleY(), scrollBarWidth, scrollBarHeight, this.scrollBarGrabbed ? 0xffff00ff : (this.scrollHandleHovered ? 0xff2288cc : 0xff0061a4));
    }

    protected int getScrollHandleY()
    {
        if (this.contentSize <= this.height)
            return 0;

        return this.scrollAmount * (this.height+2) / this.contentSize;
    }

    protected int getScrollHandleHeight()
    {
        if (this.contentSize <= this.height)
            return 0;

        return this.height * this.height / this.contentSize;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

}
