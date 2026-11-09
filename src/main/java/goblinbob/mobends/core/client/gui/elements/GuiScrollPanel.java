package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * A panel whose content scrolls: by the mouse wheel over it, or by dragging the scroll bar on its
 * right. The scroll eases to where it's sent, a tick at a time, and is drawn in between.
 */
public abstract class GuiScrollPanel {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int scrollBarWidth = 6;

    /** Height of the full content. */
    protected int contentSize;
    protected int scrollAmountTarget;
    protected int prevScrollAmount;
    protected int scrollAmount;
    protected boolean hovered;
    protected boolean scrollBarHovered;
    protected boolean scrollHandleHovered;
    protected boolean scrollBarGrabbed;
    protected int scrollBarGrabY;

    protected GuiScrollPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Every tick, with the mouse on the screen. */
    public void update(int mouseX, int mouseY) {
        this.hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        this.scrollBarHovered = false;
        this.scrollHandleHovered = false;

        if (this.scrollBarGrabbed) {
            this.scrollTo((mouseY - y - scrollBarGrabY) * contentSize / height);
        }

        if (this.hovered && mouseX >= this.x + this.width - this.scrollBarWidth) {
            this.scrollBarHovered = true;
            int scrollHandleY = getScrollHandleY(1);
            if (mouseY >= this.y + scrollHandleY && mouseY <= y + scrollHandleY + getScrollHandleHeight()) {
                this.scrollHandleHovered = true;
            }
        }

        this.prevScrollAmount = this.scrollAmount;
        this.scrollAmount += (int) ((this.scrollAmountTarget - this.scrollAmount) * getScrollTweenSpeed());
    }

    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        this.scrollBarGrabbed = false;
        if (scrollBarHovered) {
            scrollBarGrabY = scrollHandleHovered ? (int) mouseY - y - getScrollHandleY(1) : getScrollHandleHeight() / 2;
            scrollBarGrabbed = true;
            return true;
        }
        return false;
    }

    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        this.scrollBarGrabbed = false;
        return false;
    }

    /** The mouse wheel, {@code amount} up (positive) or down. */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.hovered && amount != 0) {
            this.scroll((amount > 0 ? -1 : 1) * getScrollSpeed());
            return true;
        }
        return false;
    }

    protected void scrollTo(int value) {
        if (contentSize <= height) {
            this.scrollAmountTarget = 0;
            return;
        }
        this.scrollAmountTarget = Mth.clamp(value, 0, contentSize - height);
    }

    protected void scroll(int amount) {
        scrollTo(this.scrollAmountTarget + amount);
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // The scroll moves tick by tick: eased by the partial tick, not by what a screen is handed
        // (the frame's real time, in ticks).
        partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        int scroll = Math.round(getScrollAmount(partialTicks));

        this.drawBackground(graphics, partialTicks);

        graphics.enableScissor(x, y, x + width - scrollBarWidth, y + height);
        this.drawContent(graphics, x, y - scroll, mouseX, mouseY, partialTicks);
        graphics.disableScissor();

        this.drawScrollBar(graphics, partialTicks);
    }

    protected void drawBackground(GuiGraphics graphics, float partialTicks) {
    }

    /** Draws the content, its top left at {@code originX}, {@code originY} (scrolled). */
    protected abstract void drawContent(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks);

    protected void drawScrollBar(GuiGraphics graphics, float partialTicks) {
        if (contentSize <= height) {
            return;
        }
        int barX = x + width - scrollBarWidth;
        // Background
        Draw.rectangle(graphics, barX, y, scrollBarWidth, height, getBackgroundColor());
        int barColor = this.scrollBarGrabbed
                ? getScrollBarGrabbedColor()
                : (this.scrollHandleHovered ? getScrollBarHoveredColor() : getScrollBarColor());
        // Handle
        Draw.rectangle(graphics, barX, y + getScrollHandleY(partialTicks), scrollBarWidth, getScrollHandleHeight(), barColor);
    }

    public int getScrollAmount() {
        return scrollAmount;
    }

    /** How far the content is scrolled, as drawn this frame. */
    public float getScrollAmount(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevScrollAmount, this.scrollAmount);
    }

    protected int getScrollHandleY(float partialTicks) {
        if (this.contentSize <= this.height) {
            return 0;
        }
        return (int) getScrollAmount(partialTicks) * (this.height + 2) / this.contentSize;
    }

    protected int getScrollHandleHeight() {
        if (this.contentSize <= this.height) {
            return 0;
        }
        return this.height * this.height / this.contentSize;
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

    protected int getScrollSpeed() {
        return 10;
    }

    protected float getScrollTweenSpeed() {
        return .5F;
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
