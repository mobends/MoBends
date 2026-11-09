package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A vanilla button of any size (one less than 20 high keeps its edges: the game's button sprite is
 * nine-sliced), placed by its owner. It does nothing when pressed: its owner asks
 * {@link #mousePressed} whether it was.
 */
public class GuiCustomButton extends Button {
    public GuiCustomButton(int width, int height, Component text) {
        super(0, 0, width, height, text, button -> {}, DEFAULT_NARRATION);
    }

    /** Whether a left click at the mouse presses it (with its sound). */
    public boolean mousePressed(double mouseX, double mouseY, int button) {
        return this.mouseClicked(mouseX, mouseY, button);
    }
}
