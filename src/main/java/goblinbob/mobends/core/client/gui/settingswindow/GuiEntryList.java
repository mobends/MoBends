package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedList;

/** The list of the order and options windows, in the Settings window's frame. */
class GuiEntryList<T extends IGuiListElement> extends GuiList<T> {
    private final LinkedList<T> elements = new LinkedList<>();

    GuiEntryList(int width, int height) {
        super(0, 0, width, height, 5, 3, 3, 15);
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, float partialTicks) {
        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, x, y, this.width, this.height, 4, 36, 117);
    }

    @Override
    public LinkedList<T> getListElements() {
        return elements;
    }
}
