package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.core.util.UIScissorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

/**
 * A panel whose content scrolls: by the mouse wheel over it, or by dragging the scroll bar on its
 * right. It scrolls as vanilla's lists do (the language list): the wheel moves it straight away,
 * {@link #getScrollSpeed} pixels a notch; the scroll bar follows the mouse every frame while it's
 * held, grabbed where it was clicked, without jumping.
 */
public abstract class GuiScrollPanel extends GuiElement
{

    protected int width;
    protected int height;
    protected int scrollBarWidth;

    /**
     * Height of the full content
     */
    protected int contentSize;
    /** How far the content is scrolled, in pixels. */
    protected float scrollAmount;
    protected boolean hovered;
    protected boolean scrollBarHovered;
    protected boolean scrollHandleHovered;
    protected boolean scrollBarGrabbed;
    /** Where the mouse held the scroll bar, from the handle's top (outside the handle if it was clicked beside it). */
    protected int scrollBarGrabY;
    /**
     * The mouse's height in the frame {@link #update} is given it in, less its height on the
     * screen: so {@link #draw} finds it every frame while the scroll bar is held.
     */
    private int mouseOffsetY;

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
        this.mouseOffsetY = mouseY - screenMouseY();

        if (this.hovered && contentSize > height && mouseX >= this.x + this.width - this.scrollBarWidth)
        {
            this.scrollBarHovered = true;

            final int scrollHandleY = getScrollHandleY();
            if (mouseY >= this.y + scrollHandleY && mouseY <= y + scrollHandleY + getScrollHandleHeight())
            {
                this.scrollHandleHovered = true;
            }
        }
    }

    /** The mouse's height on the screen, in the screen's scaled units, as GuiScreen hands it to drawScreen. */
    private static int screenMouseY()
    {
        final Minecraft mc = Minecraft.getMinecraft();
        final int height = new ScaledResolution(mc).getScaledHeight();
        return height - Mouse.getY() * height / mc.displayHeight - 1;
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        super.handleMouseClicked(mouseX, mouseY, button);

        this.scrollBarGrabbed = false;

        if (scrollBarHovered && button == 0)
        {
            // Held where it was clicked, also beside the handle: the handle follows the mouse from
            // there, without jumping to it, as vanilla's lists do.
            scrollBarGrabY = mouseY - y - getScrollHandleY();
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
                // Straight away, a step for every event of the wheel, as vanilla's lists (GuiSlot):
                // how much a notch rolls differs between platforms, so only its way counts.
                this.setScrollAmount(this.scrollAmount + (mouseWheelRoll > 0 ? 1 : -1) * getScrollSpeed());
            }
            return true;
        }

        return false;
    }

    protected int getMaxScroll()
    {
        return Math.max(0, contentSize - height);
    }

    protected void setScrollAmount(float value)
    {
        this.scrollAmount = GUtil.clamp(value, 0, getMaxScroll());
    }

    @Override
    public void draw(float partialTicks)
    {
        if (this.scrollBarGrabbed)
        {
            // Every frame, so the content keeps up with the mouse.
            final int mouseY = screenMouseY() + mouseOffsetY;
            final int track = height - getScrollHandleHeight();
            setScrollAmount(track <= 0 ? 0 : (float) (mouseY - y - scrollBarGrabY) * getMaxScroll() / track);
        }
        final int scroll = getScrollAmount();

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground(partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scroll, 0);
        UIScissorHelper.INSTANCE.push(this.getAbsoluteX(), this.getAbsoluteY(), this.width - this.scrollBarWidth, this.height);

        this.drawChildren(partialTicks);
        this.drawContent(partialTicks);

        UIScissorHelper.INSTANCE.pop();
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
        Draw.rectangle(width - scrollBarWidth, this.getScrollHandleY(), scrollBarWidth, scrollBarHeight, barColor);

        GlStateManager.color(1, 1, 1, 1);
    }

    /** How far the content is scrolled, in whole pixels, as it's drawn. */
    public int getScrollAmount()
    {
        return (int) scrollAmount;
    }

    /** Where the handle's top is, from the bar's, as vanilla's lists place it. */
    protected int getScrollHandleY()
    {
        final int maxScroll = getMaxScroll();
        if (maxScroll <= 0)
            return 0;

        return Math.max(0, getScrollAmount() * (this.height - getScrollHandleHeight()) / maxScroll);
    }

    /** As tall as the share of the content in view, as vanilla's lists, but never shorter than 32 pixels. */
    protected int getScrollHandleHeight()
    {
        if (getMaxScroll() <= 0)
            return 0;

        return GUtil.clamp(this.height * this.height / this.contentSize, Math.min(32, this.height - 8), this.height - 8);
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

    /** How far a notch of the mouse wheel scrolls, in pixels. */
    protected int getScrollSpeed() { return 10; }

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
