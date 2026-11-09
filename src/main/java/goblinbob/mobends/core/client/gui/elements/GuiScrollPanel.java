package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * A panel whose content scrolls: by the mouse wheel over it, or by dragging the scroll bar on its
 * right. It scrolls as vanilla's lists do (the language list): the wheel moves it straight away, by
 * its amount (a trackpad's fine steps too), {@link #getScrollSpeed} pixels a notch; the scroll bar
 * follows the mouse every frame while it's held, grabbed where it was clicked, without jumping.
 */
public abstract class GuiScrollPanel {
    /** The scroll bar's handle is never shorter than this, as vanilla's. */
    private static final int MIN_HANDLE_HEIGHT = 32;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int scrollBarWidth = 6;

    /** Height of the full content. */
    protected int contentSize;
    /** How far the content is scrolled, in pixels. */
    protected double scrollAmount;
    /** Whether the mouse was over the panel at the last tick (for what's drawn on hover). */
    protected boolean hovered;
    protected boolean scrollBarGrabbed;
    /** Where the mouse held the scroll bar, from the handle's top (outside the handle if it was clicked beside it). */
    protected double scrollBarGrabY;

    protected GuiScrollPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Every tick, with the mouse on the screen. */
    public void update(int mouseX, int mouseY) {
        this.hovered = isOver(mouseX, mouseY);
    }

    protected boolean isOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /** Over the scroll bar (only there when the content doesn't fit). */
    protected boolean isOverScrollBar(double mouseX, double mouseY) {
        return getMaxScroll() > 0 && isOver(mouseX, mouseY) && mouseX >= x + width - scrollBarWidth;
    }

    private boolean isOverScrollHandle(double mouseX, double mouseY) {
        int handleY = y + getScrollHandleY();
        return isOverScrollBar(mouseX, mouseY) && mouseY >= handleY && mouseY <= handleY + getScrollHandleHeight();
    }

    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        this.scrollBarGrabbed = false;
        if (button == 0 && isOverScrollBar(mouseX, mouseY)) {
            scrollBarGrabY = mouseY - y - getScrollHandleY();
            scrollBarGrabbed = true;
            return true;
        }
        return false;
    }

    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        this.scrollBarGrabbed = false;
        return false;
    }

    /** The mouse wheel, {@code amount} notches up (positive) or down; a trackpad sends fractions. */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount != 0 && isOver(mouseX, mouseY)) {
            setScrollAmount(scrollAmount - amount * getScrollSpeed());
            return true;
        }
        return false;
    }

    protected int getMaxScroll() {
        return Math.max(0, contentSize - height);
    }

    protected void setScrollAmount(double value) {
        this.scrollAmount = Mth.clamp(value, 0, getMaxScroll());
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (scrollBarGrabbed) {
            // Every frame, so the content keeps up with the mouse.
            int track = height - getScrollHandleHeight();
            setScrollAmount(track <= 0 ? 0 : (mouseY - y - scrollBarGrabY) * getMaxScroll() / track);
        }
        int scroll = getScrollAmount();

        this.drawBackground(graphics, partialTicks);

        graphics.enableScissor(x, y, x + width - scrollBarWidth, y + height);
        this.drawContent(graphics, x, y - scroll, mouseX, mouseY, partialTicks);
        graphics.disableScissor();

        this.drawScrollBar(graphics, mouseX, mouseY);
    }

    protected void drawBackground(GuiGraphics graphics, float partialTicks) {
    }

    /** Draws the content, its top left at {@code originX}, {@code originY} (scrolled). */
    protected abstract void drawContent(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks);

    protected void drawScrollBar(GuiGraphics graphics, int mouseX, int mouseY) {
        if (getMaxScroll() <= 0) {
            return;
        }
        int barX = x + width - scrollBarWidth;
        // Background
        Draw.rectangle(graphics, barX, y, scrollBarWidth, height, getBackgroundColor());
        int barColor = this.scrollBarGrabbed
                ? getScrollBarGrabbedColor()
                : (isOverScrollHandle(mouseX, mouseY) ? getScrollBarHoveredColor() : getScrollBarColor());
        // Handle
        Draw.rectangle(graphics, barX, y + getScrollHandleY(), scrollBarWidth, getScrollHandleHeight(), barColor);
    }

    /** How far the content is scrolled, in whole pixels, as it's drawn. */
    public int getScrollAmount() {
        return (int) scrollAmount;
    }

    /** Where the handle's top is, from the bar's, as vanilla places it. */
    protected int getScrollHandleY() {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) {
            return 0;
        }
        return Math.max(0, (int) scrollAmount * (height - getScrollHandleHeight()) / maxScroll);
    }

    /** As tall as the share of the content in view, as vanilla's, but never shorter than 32 pixels. */
    protected int getScrollHandleHeight() {
        if (getMaxScroll() <= 0) {
            return 0;
        }
        return Mth.clamp(height * height / contentSize, Math.min(MIN_HANDLE_HEIGHT, height - 8), height - 8);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /** How far a notch of the mouse wheel scrolls, in pixels. */
    protected int getScrollSpeed() {
        return 10;
    }

    protected int getBackgroundColor() {
        return 0xff111111;
    }

    protected int getScrollBarColor() {
        return 0xff888888;
    }

    protected int getScrollBarHoveredColor() {
        return 0xff999999;
    }

    protected int getScrollBarGrabbedColor() {
        return 0xffbbbbbb;
    }
}
