package goblinbob.mobends.core.client.gui.supporterswindow;

import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.function.BooleanSupplier;

/**
 * A row of the Supporters window's list: the accessory's name, and whether the player has it (a
 * lock if they don't, which tells what it means when hovered). Clicking it shows the accessory in
 * the window's preview; the row shown is highlighted.
 */
public class GuiAccessoryRow implements IGuiListElement
{

    private static final int HEIGHT = 20;
    /** The lock of an accessory the player doesn't have, in GuiBenderList.LIST_TEXTURE. */
    private static final int LOCK_U = 80, LOCK_V = 120, LOCK_WIDTH = 8, LOCK_HEIGHT = 10;
    /** How far past the lock the mouse still hovers it, so a small icon isn't fiddly to find. */
    private static final int LOCK_HOVER_MARGIN = 2;

    private final GuiAccessoryList list;
    private final String name;
    private final AccessorySettings settings;
    private final BooleanSupplier selected;
    private final Runnable onSelect;
    private final Minecraft mc;

    private int x, y;
    private int width;
    private boolean hovered;
    private boolean lockHovered;
    private int listOrder;

    /**
     * @param settings What the player has of the accessory.
     * @param selected Whether the preview shows this accessory.
     * @param onSelect Shows this accessory in the preview.
     */
    public GuiAccessoryRow(GuiAccessoryList list, String name, AccessorySettings settings, BooleanSupplier selected, Runnable onSelect)
    {
        this.list = list;
        this.name = name;
        this.settings = settings;
        this.selected = selected;
        this.onSelect = onSelect;
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.width = list.getRowWidth();
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int state)
    {
        if (!hovered || state != 0)
        {
            return false;
        }
        if (!selected.getAsBoolean())
        {
            GuiHelper.playButtonSound(mc.getSoundHandler());
            onSelect.run();
        }
        return true;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEIGHT;
        this.lockHovered = !settings.isUnlocked()
                && mouseX >= lockX() - LOCK_HOVER_MARGIN && mouseX < lockX() + LOCK_WIDTH + LOCK_HOVER_MARGIN
                && mouseY >= lockY() - LOCK_HOVER_MARGIN && mouseY < lockY() + LOCK_HEIGHT + LOCK_HOVER_MARGIN;
    }

    /** Whether the mouse is over the lock (so the window shows what it means); false if there is none. */
    public boolean isLockHovered()
    {
        return lockHovered;
    }

    @Override
    public void draw(float partialTicks)
    {
        final boolean isSelected = selected.getAsBoolean();

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
        Draw.borderBox(x + 4, y + 4, width - 8, HEIGHT - 8, 4, isSelected || hovered ? 140 : 130, 0);
        if (isSelected)
        {
            GlStateManager.disableTexture2D();
            Draw.rectangle(x, y, 2, HEIGHT, 0xffd8306a);
            GlStateManager.enableTexture2D();
        }

        final int textY = y + (HEIGHT - 8) / 2;
        final int statusWidth;
        if (settings.isUnlocked())
        {
            final String status = I18n.format(settings.isHidden() ? "mobends.gui.supporters.hidden" : "mobends.gui.supporters.unlocked");
            statusWidth = mc.fontRendererObj.getStringWidth(status);
            mc.fontRendererObj.drawString(status, x + width - 6 - statusWidth, textY, 0x6fd35a);
        }
        else
        {
            statusWidth = LOCK_WIDTH;
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
            Draw.texturedModalRect(lockX(), lockY(), LOCK_U, LOCK_V, LOCK_WIDTH, LOCK_HEIGHT);
        }
        mc.fontRendererObj.drawStringWithShadow(mc.fontRendererObj.trimStringToWidth(name, width - 12 - statusWidth - 6),
                x + 6, textY, 0xffffff);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private int lockX()
    {
        return x + width - 6 - LOCK_WIDTH;
    }

    private int lockY()
    {
        return y + (HEIGHT - LOCK_HEIGHT) / 2;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getHeight()
    {
        return HEIGHT;
    }

    @Override
    public int getOrder()
    {
        return listOrder;
    }

    @Override
    public void setOrder(int order)
    {
        listOrder = order;
    }

}
