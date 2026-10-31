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
 * A row of the Supporters window's list: the accessory's name, and whether the player has it.
 * Clicking it shows the accessory in the window's preview; the row shown is highlighted.
 */
public class GuiAccessoryRow implements IGuiListElement
{

    private static final int HEIGHT = 20;

    private final GuiAccessoryList list;
    private final String name;
    private final AccessorySettings settings;
    private final BooleanSupplier selected;
    private final Runnable onSelect;
    private final Minecraft mc;

    private int x, y;
    private int width;
    private boolean hovered;
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

        final String status = status();
        final int statusWidth = mc.fontRenderer.getStringWidth(status);
        final int textY = y + (HEIGHT - 8) / 2;
        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(name, width - 12 - statusWidth - 6),
                x + 6, textY, 0xffffff);
        mc.fontRenderer.drawString(status, x + width - 6 - statusWidth, textY, settings.isUnlocked() ? 0x6fd35a : 0xe0a83a);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private String status()
    {
        if (!settings.isUnlocked())
            return I18n.format("mobends.gui.supporters.locked");
        return I18n.format(settings.isHidden() ? "mobends.gui.supporters.hidden" : "mobends.gui.supporters.unlocked");
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
