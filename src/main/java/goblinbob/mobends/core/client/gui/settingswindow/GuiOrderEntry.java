package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** An entry of an order list: its id, a description, and buttons to move it up or down. */
public class GuiOrderEntry implements IGuiListElement {
    private static final int BUTTON_SIZE = 20;

    private final String id;
    private final String description;
    private final int position;
    private final Runnable onMoveUp;
    private final Runnable onMoveDown;
    private final GuiCustomButton upButton;
    private final GuiCustomButton downButton;

    private int x;
    private int y;
    private int listOrder;

    public GuiOrderEntry(String id, String description, int position, int count, Runnable onMoveUp, Runnable onMoveDown) {
        this.id = id;
        this.description = description;
        this.position = position;
        this.onMoveUp = onMoveUp;
        this.onMoveDown = onMoveDown;
        this.upButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, Component.literal("▲"));
        this.downButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, Component.literal("▼"));
        this.upButton.active = position > 0;
        this.downButton.active = position < count - 1;
    }

    @Override
    public void initGui(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void layOut(int originX, int originY) {
        this.downButton.setPosition(originX + x + 232, originY + y + 5);
        this.upButton.setPosition(originX + x + 232 - BUTTON_SIZE - 2, originY + y + 5);
    }

    @Override
    public boolean handleMouseClicked(int originX, int originY, double mouseX, double mouseY, int button) {
        layOut(originX, originY);
        if (upButton.mousePressed(mouseX, mouseY, button)) {
            onMoveUp.run();
            return true;
        }
        if (downButton.mousePressed(mouseX, mouseY, button)) {
            onMoveDown.run();
            return true;
        }
        return false;
    }

    @Override
    public void update(int mouseX, int mouseY) {
    }

    @Override
    public void draw(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        layOut(originX, originY);
        Font font = Minecraft.getInstance().font;
        int left = originX + x;
        int top = originY + y;
        graphics.drawString(font, (position + 1) + ".", left + 4, top + 6, 0xaaaaaa);
        graphics.drawString(font, font.plainSubstrByWidth(id, 170), left + 20, top + 6, 0xffffff);
        graphics.drawString(font, font.plainSubstrByWidth(description, 170), left + 20, top + 18, 0x9a9a9a, false);

        upButton.render(graphics, mouseX, mouseY, partialTicks);
        downButton.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    @Override
    public int getOrder() {
        return listOrder;
    }

    @Override
    public void setOrder(int order) {
        listOrder = order;
    }
}
