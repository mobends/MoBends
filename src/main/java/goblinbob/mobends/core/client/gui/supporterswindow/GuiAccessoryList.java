package goblinbob.mobends.core.client.gui.supporterswindow;

import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedList;

/** The accessories of the Supporters window, drawn like the Settings window's list of mobs. */
public class GuiAccessoryList extends GuiList<GuiAccessoryRow> {
    private final LinkedList<GuiAccessoryRow> elements = new LinkedList<>();

    public GuiAccessoryList(int x, int y, int width, int height) {
        super(x, y, width, height, 3, 3, 3, 3);
    }

    /** How wide a row is: the list but its padding and the scroll bar. */
    public int getRowWidth() {
        return this.width - this.scrollBarWidth - 2 * this.paddingLeft;
    }

    /** Whether the mouse is over the lock of a row, within the list's view. */
    public boolean isLockHovered() {
        return this.hovered && elements.stream().anyMatch(GuiAccessoryRow::isLockHovered);
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, float partialTicks) {
        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, x, y, this.width, this.height, 4, 36, 117);
    }

    @Override
    public LinkedList<GuiAccessoryRow> getListElements() {
        return elements;
    }

    @Override
    protected int getScrollSpeed() {
        return 20;
    }
}
