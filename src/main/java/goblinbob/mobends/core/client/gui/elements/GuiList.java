package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** A scrolling list of elements, one under the other. */
public abstract class GuiList<T extends IGuiListElement> extends GuiScrollPanel {
    protected int spacing;
    protected int paddingLeft;
    protected int paddingTop;
    protected int paddingBottom;

    protected GuiList(int x, int y, int width, int height, int spacing, int paddingLeft, int paddingTop, int paddingBottom) {
        super(x, y, width, height);
        this.spacing = spacing;
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    public abstract LinkedList<T> getListElements();

    public void initGui(int x, int y) {
        this.x = x;
        this.y = y;

        int yOffset = paddingTop;
        for (T entry : this.getListElements()) {
            entry.initGui(paddingLeft, yOffset);
            yOffset += entry.getHeight() + spacing;
        }
        yOffset -= spacing;

        this.contentSize = yOffset + paddingBottom;

        if (contentSize <= height) {
            this.scrollAmountTarget = 0;
        } else if (this.scrollAmountTarget + this.height > contentSize) {
            this.scrollAmountTarget = contentSize - this.height;
            this.scrollAmount = contentSize - this.height;
        }
    }

    public void addElement(T element) {
        List<T> elements = getListElements();
        elements.add(element);
        element.setOrder(elements.size() - 1);
        this.initGui(this.x, this.y);
    }

    public void clearElements() {
        this.getListElements().clear();
        this.initGui(this.x, this.y);
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);
        for (T element : this.getListElements()) {
            element.update(mouseX - x, mouseY - y + scrollAmount);
        }
    }

    @Override
    protected void drawContent(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        for (T element : this.getListElements()) {
            if (isVisible(element, originY)) {
                element.draw(graphics, originX, originY, mouseX, mouseY, partialTicks);
            }
        }
    }

    /** Whether any of the element is in view, with the content's top at {@code originY}. */
    protected boolean isVisible(T element, int originY) {
        int top = originY + element.getY();
        return top + element.getHeight() > y && top < y + height;
    }

    @Override
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered && !this.scrollBarHovered) {
            int originY = y - scrollAmount;
            Iterator<T> it = this.getListElements().descendingIterator();
            while (it.hasNext()) {
                T element = it.next();
                if (isVisible(element, originY) && element.handleMouseClicked(x, originY, mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return super.handleMouseClicked(mouseX, mouseY, button);
    }
}
