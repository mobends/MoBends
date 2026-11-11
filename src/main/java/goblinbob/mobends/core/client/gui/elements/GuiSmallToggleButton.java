package goblinbob.mobends.core.client.gui.elements;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
//? if >=1.20.2 {
import goblinbob.mobends.core.util.Ids;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
//? if >=1.20.2 {
import net.minecraft.resources.ResourceLocation;
//?}

/** A small switch: the game's button, green and ON, or red and OFF. Clicking it flips it. */
public class GuiSmallToggleButton extends AbstractButton {
    //? if >=1.20.2 {
    private static final ResourceLocation BUTTON = Ids.minecraft("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = Ids.minecraft("widget/button_highlighted");
    //?}
    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;

    protected boolean toggleState;

    public GuiSmallToggleButton() {
        super(0, 0, WIDTH, HEIGHT, Component.empty());
    }

    public void initGui(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void onPress() {
        this.toggleState = !this.toggleState;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        if (this.toggleState) {
            graphics.setColor(0.3F, 1.0F, 0.7F, 1.0F);
        } else {
            graphics.setColor(1.0F, 0.3F, 0.4F, 1.0F);
        }
        //? if >=1.20.2 {
        graphics.blitSprite(this.isHoveredOrFocused() ? BUTTON_HIGHLIGHTED : BUTTON, getX(), getY(), WIDTH, HEIGHT);
        //?} else {
        /*// The button and its highlighted look, from the widgets texture, as 1.20.1's buttons draw them.
        int textureY = 46 + (this.isHoveredOrFocused() ? 2 : 1) * 20;
        graphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), WIDTH, HEIGHT, 20, 4, 200, 20, 0, textureY);
        *///?}
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        int color = !this.active ? 0xA0A0A0 : this.isHovered() ? 0xFFFFA0 : 0xE0E0E0;
        String stateText = this.toggleState ? "ON" : "OFF";
        graphics.drawString(font, stateText, getX() - font.width(stateText) / 2 + WIDTH / 2, getY() + (HEIGHT - 8) / 2, color, false);
    }

    public void setToggleState(boolean state) {
        this.toggleState = state;
    }

    public boolean getToggleState() {
        return this.toggleState;
    }

    /** Whether a left click at the mouse flips it (with the button's sound). */
    public boolean mousePressed(double mouseX, double mouseY, int button) {
        return this.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.toggleState ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
}
//?}
