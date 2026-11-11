package goblinbob.mobends.core.client.gui.elements;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;

/**
 * An element of a {@link GuiList}. It's laid out in the list's content ({@link #initGui}, the
 * list's top left being 0, 0), and drawn and clicked on the screen, the list's content starting at
 * {@code originX}, {@code originY} (scrolled).
 */
public interface IGuiListElement {
    void initGui(int x, int y);

    /** Every tick, with the mouse in the list's content. */
    void update(int mouseX, int mouseY);

    void draw(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks);

    boolean handleMouseClicked(int originX, int originY, double mouseX, double mouseY, int button);

    int getX();

    int getY();

    int getHeight();

    int getOrder();

    void setOrder(int order);
}
//?}
