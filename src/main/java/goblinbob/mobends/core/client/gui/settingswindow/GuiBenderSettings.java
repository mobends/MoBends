package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.client.preview.EntityPreview;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.UIScissorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * A row of the Settings window: the mob animated in its frame, its name and where it comes from,
 * the toggle that animates it, and the buttons that order its types and extensions. The mob walks
 * while the row is hovered, and the row is dimmed while the mob isn't animated.
 */
public class GuiBenderSettings implements IGuiListElement
{

    private static final int HEIGHT = 40;
    private static final int FRAME_SIZE = 36;
    /** Inside the frame's 2px border, what the mob is clipped to. */
    private static final int THUMBNAIL_SIZE = FRAME_SIZE - 4;
    /** The hitbox fits this, leaving a little room around it. */
    private static final int MOB_SIZE = THUMBNAIL_SIZE - 4;
    private static final int TEXT_X = FRAME_SIZE + 6;
    private static final int BUTTON_HEIGHT = 14;

    private final GuiBenderList list;
    private final EntityBender<?> bender;
    private final EntityPreview preview;
    private final String source;
    private final Minecraft mc;
    private final GuiSmallToggleButton toggleButton;
    /** Opens the order of the entity's types; only there when more than one can animate it. */
    private final GuiCustomButton orderButton;
    private final Runnable onOpenOrder;
    /** Opens the order of the extensions of those types; only there when they have more than one. */
    private final GuiCustomButton extensionsButton;
    private final Runnable onOpenExtensions;

    private int x, y;
    private int width;
    private int mouseX, mouseY;
    private boolean hovered;
    private int listOrder;

    public GuiBenderSettings(GuiBenderList list, EntityBender<?> bender, EntityPreview preview,
                             int typeCount, Runnable onOpenOrder, int extensionCount, Runnable onOpenExtensions)
    {
        this.list = list;
        this.bender = bender;
        this.preview = preview;
        this.source = sourceOf(bender);
        this.mc = Minecraft.getMinecraft();
        this.toggleButton = new GuiSmallToggleButton();
        this.toggleButton.setToggleState(bender.isAnimated());
        this.orderButton = typeCount > 1 ? compactButton(I18n.format("mobends.gui.typeorder.button", typeCount)) : null;
        this.onOpenOrder = onOpenOrder;
        this.extensionsButton = extensionCount > 1 ? compactButton(I18n.format("mobends.gui.extensionorder.button", extensionCount)) : null;
        this.onOpenExtensions = onOpenExtensions;
    }

    private GuiCustomButton compactButton(String text)
    {
        // An even width, so the button's halves meet.
        final int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        return new GuiCustomButton((textWidth + 11) & ~1, BUTTON_HEIGHT, text);
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.width = list.getRowWidth();
        this.toggleButton.initGui(x + width - 34, y + 10);
        int buttonX = x + TEXT_X;
        if (orderButton != null)
        {
            this.orderButton.setPosition(buttonX, y + 23);
            buttonX += orderButton.width + 3;
        }
        if (extensionsButton != null)
            this.extensionsButton.setPosition(buttonX, y + 23);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(toggleButton.getToggleState());
            return true;
        }

        if (orderButton != null && mouseButton == 0 && orderButton.mousePressed(mouseX, mouseY))
        {
            onOpenOrder.run();
            return true;
        }

        if (extensionsButton != null && mouseButton == 0 && extensionsButton.mousePressed(mouseX, mouseY))
        {
            onOpenExtensions.run();
            return true;
        }

        return false;
    }

    public void update(int mouseX, int mouseY)
    {
        toggleButton.update(mouseX, mouseY);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEIGHT;
        preview.tick(hovered);
    }

    public void draw(float partialTicks)
    {
        final boolean animated = bender.isAnimated();

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
        Draw.borderBox(x + 4, y + 4, width - 8, HEIGHT - 8, 4, hovered ? 140 : 130, 0);
        Draw.borderBox(x + 4, y + 4, FRAME_SIZE - 4, FRAME_SIZE - 4, 2, 150, 0);

        drawThumbnail(animated, partialTicks);

        final int textWidth = width - 34 - 4 - TEXT_X;
        final boolean hasButtons = orderButton != null || extensionsButton != null;
        final int nameY = y + (hasButtons ? 4 : 10);
        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(bender.getLocalizedName(), textWidth),
                x + TEXT_X, nameY, animated ? 0xffffff : 0x8a8a8a);
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(source, textWidth),
                x + TEXT_X, nameY + 11, animated ? 0x9a9a9a : 0x626262);

        toggleButton.draw();
        if (orderButton != null)
            orderButton.drawButton(mouseX, mouseY, partialTicks);
        if (extensionsButton != null)
            extensionsButton.drawButton(mouseX, mouseY, partialTicks);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private void drawThumbnail(boolean animated, float partialTicks)
    {
        final int left = x + 2 + 2;
        final int top = y + 2 + 2;
        GlStateManager.disableTexture2D();
        Draw.rectangleVerticalGradient(left, top, THUMBNAIL_SIZE, THUMBNAIL_SIZE, 0xff2c2c2c, 0xff171717);

        // The list's content is drawn scrolled; the clip is on the screen.
        UIScissorHelper.INSTANCE.push(list.getAbsoluteX() + left, list.getAbsoluteY() - list.getScrollAmount(partialTicks) + top,
                THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(left + THUMBNAIL_SIZE / 2F, top + THUMBNAIL_SIZE / 2F, 0F);
        preview.draw(MOB_SIZE, partialTicks);
        GlStateManager.popMatrix();

        if (!animated)
        {
            GlStateManager.disableDepth();
            Draw.rectangle(left, top, THUMBNAIL_SIZE, THUMBNAIL_SIZE, 0x80000000);
            GlStateManager.enableDepth();
        }
        UIScissorHelper.INSTANCE.pop();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    /**
     * The mod that adds the mob, followed by the resource pack that animates it ("Minecraft · from
     * Some Pack"), or else by the mod that does ("Twilight Forest · via Some Addon") unless that is
     * the same mod, or Mo' Bends.
     */
    private static String sourceOf(EntityBender<?> bender)
    {
        final Class<? extends EntityLivingBase> entityClass = EntityPreview.concreteClassOf(bender.entityClass);
        final ResourceLocation entityId = entityClass == null ? null : EntityList.getKey(entityClass);
        final String entityMod = entityId == null ? "minecraft" : entityId.getResourceDomain();
        if (bender.getResourcePack() != null)
        {
            return I18n.format("mobends.gui.settings.from_pack", modName(entityMod), bender.getResourcePack());
        }
        final String benderMod = new ResourceLocation(bender.getKey()).getResourceDomain();
        if (benderMod.equals(ModStatics.MODID) || benderMod.equals(entityMod))
        {
            return modName(entityMod);
        }
        return I18n.format("mobends.gui.settings.via", modName(entityMod), modName(benderMod));
    }

    private static String modName(String modId)
    {
        final ModContainer mod = Loader.instance().getIndexedModList().get(modId);
        if (mod != null)
            return mod.getName();
        return "minecraft".equals(modId) ? "Minecraft" : modId;
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
