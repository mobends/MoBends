package net.gobbob.mobends.core.client.gui.elements;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.UIScissorHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

public abstract class GuiScrollPanel extends GuiElement
{

    protected int width;
    protected int height;
    protected int scrollBarWidth;

    /**
     * Height of the full content
     */
    protected int contentSize;
    protected int scrollAmountTarget;
    protected int prevScrollAmount;
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
        this.scrollAmountTarget = 0;
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
            this.scrollTo((int) (mouseY - y - scrollBarGrabY) * (contentSize) / height);
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

        this.prevScrollAmount = this.scrollAmount;
        this.scrollAmount += (this.scrollAmountTarget - this.scrollAmount) * getScrollTweenSpeed();
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        super.handleMouseClicked(mouseX, mouseY, button);

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

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        this.scrollBarGrabbed = false;

        return false;
    }

    public boolean handleMouseInput()
    {
        int mouseWheelRoll = -Mouse.getEventDWheel();
        if (this.hovered)
        {
            if (mouseWheelRoll != 0)
            {
                mouseWheelRoll = mouseWheelRoll > 0 ? 1 : -1;
                this.scroll(mouseWheelRoll * getScrollSpeed());
            }
            return true;
        }

        return false;
    }

    protected void scrollTo(int value)
    {
        if (contentSize <= height)
        {
            this.scrollAmountTarget = 0;
            return;
        }

        this.scrollAmountTarget = value;

        if (this.scrollAmountTarget < 0)
            this.scrollAmountTarget = 0;
        else if (this.scrollAmountTarget > contentSize - height)
            this.scrollAmountTarget = contentSize - height;
    }

    protected void scroll(int amount)
    {
        scrollTo(this.scrollAmountTarget + amount);
    }

    @Override
    public void draw(float partialTicks)
    {
        final float scroll = GUtil.lerp(this.prevScrollAmount, this.scrollAmount, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground(partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scroll, 0);
        UIScissorHelper.INSTANCE.setUIBounds((int) this.getAbsoluteX(), (int) this.getAbsoluteY(), this.width - this.scrollBarWidth, this.height);
        UIScissorHelper.INSTANCE.enable();

        this.drawChildren(partialTicks);
        this.drawContent(partialTicks);

        UIScissorHelper.INSTANCE.disable();
        GlStateManager.popMatrix();

        this.drawForeground(partialTicks);

        GlStateManager.popMatrix();
    }

    protected abstract void drawContent(float partialTicks);

    @Override
    protected void drawForeground(float partialTicks)
    {
        this.drawScrollBar(partialTicks);
    }

    protected void drawScrollBar(float partialTicks)
    {
        if (contentSize <= height)
            return;

        final int scrollBarHeight = this.getScrollHandleHeight();

        // Background
        Draw.rectangle(width - scrollBarWidth, 0, scrollBarWidth, height, this.getBackgroundColor());

        final int barColor = this.scrollBarGrabbed
                ? getScrollBarGrabbedColor()
                : (this.scrollHandleHovered ? this.getScrollBarHoveredColor() : this.getScrollBarColor());

        // Handle
        Draw.rectangle(width - scrollBarWidth, this.getScrollHandleY(partialTicks), scrollBarWidth, scrollBarHeight, barColor);

        GlStateManager.color(1, 1, 1, 1);
    }

    public int getScrollAmount()
    {
        return scrollAmount;
    }

    protected int getScrollHandleY()
    {
        return getScrollHandleY(1);
    }

    protected int getScrollHandleY(float partialTicks)
    {
        if (this.contentSize <= this.height)
            return 0;

        final int scroll = (int) GUtil.lerp(this.prevScrollAmount, this.scrollAmount, partialTicks);
        return scroll * (this.height + 2) / this.contentSize;
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

    protected int getScrollSpeed() { return 10; }

    protected float getScrollTweenSpeed()
    {
        return .5F;
    }

    protected int getBackgroundColor()
    {
        return 0xff111111;
    }

    protected int getScrollBarColor()
    {
        return 0xff888888;
    }

    protected int getScrollBarHoveredColor()
    {
        return 0xff999999;
    }

    protected int getScrollBarGrabbedColor()
    {
        return 0xffbbbbbb;
    }

}
