package goblinbob.mobends.core.client.gui.supporterswindow;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.function.BooleanSupplier;

/**
 * A row of the Supporters window's list: the accessory's name, and whether the player has it (a
 * lock if they don't, which tells what it means when hovered). Clicking it shows the accessory in
 * the window's preview; the row shown is highlighted.
 */
public class GuiAccessoryRow implements IGuiListElement {
    private static final int HEIGHT = 20;
    /** The lock of an accessory the player doesn't have, in GuiBenderList.LIST_TEXTURE. */
    private static final int LOCK_U = 80;
    private static final int LOCK_V = 120;
    private static final int LOCK_WIDTH = 8;
    private static final int LOCK_HEIGHT = 10;
    /** How far past the lock the mouse still hovers it, so a small icon isn't fiddly to find. */
    private static final int LOCK_HOVER_MARGIN = 2;

    private final GuiAccessoryList list;
    private final String name;
    private final AccessorySettings settings;
    private final BooleanSupplier selected;
    private final Runnable onSelect;
    private final Minecraft mc;

    private int x;
    private int y;
    private int width;
    private boolean hovered;
    private boolean lockHovered;
    private int listOrder;

    /**
     * @param settings What the player has of the accessory.
     * @param selected Whether the preview shows this accessory.
     * @param onSelect Shows this accessory in the preview.
     */
    public GuiAccessoryRow(GuiAccessoryList list, String name, AccessorySettings settings, BooleanSupplier selected, Runnable onSelect) {
        this.list = list;
        this.name = name;
        this.settings = settings;
        this.selected = selected;
        this.onSelect = onSelect;
        this.mc = Minecraft.getInstance();
    }

    @Override
    public void initGui(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = list.getRowWidth();
    }

    @Override
    public boolean handleMouseClicked(int originX, int originY, double mouseX, double mouseY, int button) {
        int rowX = originX + x;
        int rowY = originY + y;
        if (button != 0 || mouseX < rowX || mouseX >= rowX + width || mouseY < rowY || mouseY >= rowY + HEIGHT) {
            return false;
        }
        if (!selected.getAsBoolean()) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onSelect.run();
        }
        return true;
    }

    @Override
    public void update(int mouseX, int mouseY) {
        this.hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEIGHT;
        this.lockHovered = !settings.unlocked()
                && mouseX >= lockX(x) - LOCK_HOVER_MARGIN && mouseX < lockX(x) + LOCK_WIDTH + LOCK_HOVER_MARGIN
                && mouseY >= lockY(y) - LOCK_HOVER_MARGIN && mouseY < lockY(y) + LOCK_HEIGHT + LOCK_HOVER_MARGIN;
    }

    /** Whether the mouse is over the lock (so the window shows what it means); false if there is none. */
    public boolean isLockHovered() {
        return lockHovered;
    }

    @Override
    public void draw(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        boolean isSelected = selected.getAsBoolean();
        int rowX = originX + x;
        int rowY = originY + y;
        Font font = mc.font;

        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, rowX + 4, rowY + 4, width - 8, HEIGHT - 8, 4, isSelected || hovered ? 140 : 130, 0);
        if (isSelected) {
            Draw.rectangle(graphics, rowX, rowY, 2, HEIGHT, 0xffd8306a);
        }

        int textY = rowY + (HEIGHT - 8) / 2;
        int statusWidth;
        if (settings.unlocked()) {
            Component status = Component.translatable(settings.hidden() ? "mobends.gui.supporters.hidden" : "mobends.gui.supporters.unlocked");
            statusWidth = font.width(status);
            graphics.drawString(font, status, rowX + width - 6 - statusWidth, textY, 0x6fd35a, false);
        } else {
            statusWidth = LOCK_WIDTH;
            Draw.texturedModalRect(graphics, GuiBenderList.LIST_TEXTURE, lockX(rowX), lockY(rowY), LOCK_U, LOCK_V, LOCK_WIDTH, LOCK_HEIGHT);
        }
        graphics.drawString(font, font.plainSubstrByWidth(name, width - 12 - statusWidth - 6), rowX + 6, textY, 0xffffff);
    }

    private int lockX(int rowX) {
        return rowX + width - 6 - LOCK_WIDTH;
    }

    private static int lockY(int rowY) {
        return rowY + (HEIGHT - LOCK_HEIGHT) / 2;
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
        return HEIGHT;
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
//?}
