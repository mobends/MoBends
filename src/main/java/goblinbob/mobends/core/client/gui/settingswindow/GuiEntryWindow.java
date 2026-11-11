package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.Screens;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}
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

        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> Screens.open(new GuiSettingsWindow(exit)))
                .bounds(10, height - 30, 60, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.reset"), button -> reset())
                .bounds(width - 70, height - 30, 60, 20).build());
        entryList.initGui(this.x + 9, this.y + 9);
    }

    @Override
    //? if >=26.1 {
    /*public void extractRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    *///?} else {
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //? if <1.20.2 {
        /*renderBackground(graphics, mouseX, mouseY, partialTicks);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTicks);
    //?}
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    /** The window under the widgets. */
    //? if >=26.1 {
    /*@Override
    public void extractBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
    *///?} else if >=1.20.2 {
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
        graphics.drawString(this.font, this.font.plainSubstrByWidth(this.title.getString(), GuiSettingsWindow.EDITOR_WIDTH - 24), this.x + 6, this.y - 9, 0xFFFFFFFF);
        graphics.drawString(this.font, this.font.plainSubstrByWidth(getHint().getString(), textWidth),
                this.x + 9, this.y + GuiSettingsWindow.EDITOR_HEIGHT + 12, 0xFFCCCCCC);
    }

    @Override
    public void tick() {
        super.tick();
        entryList.update(mouseX, mouseY);
    }

    //? if >=26.1 {
    /*@Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick) || entryList.handleMouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        entryList.handleMouseReleased(event.x(), event.y(), event.button());
        return super.mouseReleased(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || entryList.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        entryList.handleMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }
    //?}

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
        Screens.open(exit);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
