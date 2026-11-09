package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;

/** The Settings window's list of mobs. Rows scrolled out of view aren't drawn, so their mobs aren't rendered. */
public class GuiBenderList extends GuiList<GuiBenderSettings> {
    /** The list's frame and rows. */
    public static final ResourceLocation LIST_TEXTURE = GuiSettingsWindow.BACKGROUND_TEXTURE;

    private final LinkedList<GuiBenderSettings> elements = new LinkedList<>();

    public GuiBenderList(int x, int y, int width, int height) {
        super(x, y, width, height, 3, 3, 3, 3);
    }

    /** How wide a row is: the list but its padding and the scroll bar. */
    public int getRowWidth() {
        return this.width - this.scrollBarWidth - 2 * this.paddingLeft;
    }

    @Override
    protected void drawBackground(GuiGraphics graphics, float partialTicks) {
        Draw.borderBox(graphics, LIST_TEXTURE, x, y, this.width, this.height, 4, 36, 117);
    }

    @Override
    public LinkedList<GuiBenderSettings> getListElements() {
        return elements;
    }

    @Override
    protected int getScrollSpeed() {
        return 20;
    }
}
