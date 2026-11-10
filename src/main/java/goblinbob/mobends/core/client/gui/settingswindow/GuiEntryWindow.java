package goblinbob.mobends.core.client.gui.settingswindow;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * A window the Settings window opens for one mob (the order of its types or extensions, its
 * options): a list in the Settings window's frame, a hint under it, Back and Reset.
 */
abstract class GuiEntryWindow<T extends IGuiListElement> extends Screen {
    protected final GuiEntryList<T> entryList = new GuiEntryList<>(GuiSettingsWindow.EDITOR_WIDTH - 10, GuiSettingsWindow.EDITOR_HEIGHT - 10);
    /** Where leaving the Mo' Bends menus goes: the game (null), or the mod list it was opened from. */
    @Nullable
    protected final Screen exit;

    private int x;
    private int y;
    private int mouseX;
    private int mouseY;

    protected GuiEntryWindow(Component title, @Nullable Screen exit) {
        super(title);
        this.exit = exit;
    }

    protected abstract Component getHint();

    protected abstract void reset();

    @Override
    protected void init() {
        this.x = (this.width - GuiSettingsWindow.EDITOR_WIDTH) / 2;
        this.y = (this.height - GuiSettingsWindow.EDITOR_HEIGHT) / 2;

        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> this.minecraft.setScreen(new GuiSettingsWindow(exit)))
                .bounds(10, height - 30, 60, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.reset"), button -> reset())
                .bounds(width - 70, height - 30, 60, 20).build());
        entryList.initGui(this.x + 9, this.y + 9);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        //? if <1.20.2 {
        /*renderBackground(graphics, mouseX, mouseY, partialTicks);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    /** The window under the widgets. */
    //? if >=1.20.2 {
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
    //?} else {
    /*private void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
    *///?}

        GuiSettingsWindow.drawWindow(graphics, x, y, GuiSettingsWindow.EDITOR_WIDTH, GuiSettingsWindow.EDITOR_HEIGHT);
        entryList.draw(graphics, mouseX, mouseY, partialTicks);

        int textWidth = GuiSettingsWindow.EDITOR_WIDTH - 12;
        graphics.drawString(this.font, this.font.plainSubstrByWidth(this.title.getString(), GuiSettingsWindow.EDITOR_WIDTH - 24), this.x + 6, this.y - 9, 0xffffff);
        graphics.drawString(this.font, this.font.plainSubstrByWidth(getHint().getString(), textWidth),
                this.x + 9, this.y + GuiSettingsWindow.EDITOR_HEIGHT + 12, 0xcccccc);
    }

    @Override
    public void tick() {
        super.tick();
        entryList.update(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return entryList.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        entryList.handleMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    //? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return entryList.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        return entryList.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    @Override
    public void onClose() {
        this.minecraft.setScreen(exit);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
//?}
