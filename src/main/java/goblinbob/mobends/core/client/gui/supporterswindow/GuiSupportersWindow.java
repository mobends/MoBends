package goblinbob.mobends.core.client.gui.supporterswindow;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.WebLink;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.client.preview.EntityPreview;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import goblinbob.mobends.core.supporters.AccessoryDetails;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.util.IColorRead;
import goblinbob.mobends.core.util.UIScissorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * The Supporters window: on the left, the player wearing the accessory picked from the list on the
 * right (every supporter accessory, whether they have it or not), turning half around now and then
 * so it shows from both sides; for the sword trail, swinging a sword in combos. Above, a thank-you to supporters or what supporting gets you; below, the link to support,
 * and a refresh of what the player has (for right after they've become a supporter). Logging in
 * is on the Link account screen of the Mo' Bends menu.
 */
public class GuiSupportersWindow extends GuiScreen
{

    private static final int EDITOR_WIDTH = GuiSettingsWindow.EDITOR_WIDTH;
    /** The window grows with the screen, leaving room for the title and back button. */
    private static final int MAX_EDITOR_HEIGHT = 280;
    private static final int SCREEN_MARGIN = 40;
    /** Inside the container's border. */
    private static final int PADDING = 5;
    private static final int INNER_WIDTH = EDITOR_WIDTH - 2 * PADDING;
    private static final int GAP = 4;
    private static final int MAX_HEADER_LINES = 3;
    private static final int PREVIEW_WIDTH = 100;
    /** How big the previewed player is drawn, of the size that fills its frame. */
    private static final float PREVIEW_SCALE = 0.85F;
    private static final int BUTTON_HEIGHT = 20;
    /** Ticks the previewed player stands before it turns half around, and takes to turn. */
    private static final int STAND_TICKS = 40;
    private static final int TURN_TICKS = 20;
    /** The accessory previewed swinging a sword: a combo of swings this many ticks apart, then the turn. */
    private static final String SWORD_TRAIL_KEY = "sword_trail";
    private static final int COMBO_SWINGS = 5;
    private static final int SWING_TICKS = 12;
    /**
     * Ticks the sword's previewed player stands after it turns. The player's animator starts the
     * combo over once 20 ticks have passed since the last swing, so the next one begins with its first.
     */
    private static final int SETTLE_TICKS = 8;
    private static final int COMPONENT_BUTTON_BACK = 0;
    private static final int COMPONENT_BUTTON_SUPPORT = 1;
    private static final int COMPONENT_BUTTON_REFRESH = 2;

    /** The container's top left; its content starts PADDING inside. */
    private int x, y;
    private int editorHeight;
    private List<String> headerLines = Collections.emptyList();
    private int bodyTop, bodyHeight;

    private final GuiAccessoryList accessoryList = new GuiAccessoryList(0, 0, INNER_WIDTH - PREVIEW_WIDTH - GAP, 100);

    /** Where the previewed players stand, closed with the window; null if it can't be made. */
    @Nullable
    private final PreviewWorld previewWorld = makePreviewWorld();
    /** A player for each accessory (by its key) dressed in it, kept while the rows are made anew. */
    private final Map<String, EntityPreview> previews = new HashMap<>();
    private final List<EntityLivingBase> dressed = new ArrayList<>();
    @Nullable
    private final EntityBender<?> playerBender = EntityBenderRegistry.instance.getDefaultBender(EntityOtherPlayerMP.class);
    private final Map<String, AccessoryDetails> accessories = new HashMap<>();

    /** The accessory previewed; null if there are none. */
    @Nullable
    private String selectedKey;
    /** The accessory last animated, and the ticks it has been, so a newly picked one starts its loop. */
    @Nullable
    private String animatedKey;
    private int animatedTicks;
    /** What the player has, as the rows were last made from it; they are made anew when it changes. */
    @Nullable
    private Map<String, AccessorySettings> ownSettings;
    private int accessoryDetailsRevision;

    public GuiSupportersWindow()
    {
        super();

        fetchAccessories();
        if (SupporterContent.getAccessories().isEmpty())
            SupporterContent.fetchAccessoryDetails();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.editorHeight = Math.min(MAX_EDITOR_HEIGHT, this.height - 2 * SCREEN_MARGIN);
        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - editorHeight) / 2;
        final int left = x + PADDING;

        // The container is drawn from (x, y), its content inset by the padding.
        this.headerLines = fontRenderer.listFormattedStringToWidth(headerMessage(), INNER_WIDTH);
        if (headerLines.size() > MAX_HEADER_LINES)
            headerLines = headerLines.subList(0, MAX_HEADER_LINES);
        // The frames' borders are drawn outside the body, over the gaps around it: they're a GAP wider.
        this.bodyTop = y + PADDING + headerLines.size() * fontRenderer.FONT_HEIGHT + 2 * GAP;
        final int buttonsY = y + editorHeight - PADDING - BUTTON_HEIGHT;
        this.bodyHeight = buttonsY - 2 * GAP - bodyTop;

        buttonList.clear();
        buttonList.add(new GuiButton(COMPONENT_BUTTON_BACK, 10, height - 30, 60, 20, I18n.format("mobends.gui.back")));
        final String refresh = I18n.format("mobends.gui.supporters.refresh");
        final int refreshWidth = fontRenderer.getStringWidth(refresh) + 20;
        final int supportWidth = INNER_WIDTH - refreshWidth - GAP;
        buttonList.add(new GuiButton(COMPONENT_BUTTON_SUPPORT, left, buttonsY, supportWidth, BUTTON_HEIGHT, I18n.format("mobends.gui.supporters.support")));
        buttonList.add(new GuiButton(COMPONENT_BUTTON_REFRESH, left + INNER_WIDTH - refreshWidth, buttonsY, refreshWidth, BUTTON_HEIGHT, refresh));

        accessoryList.setHeight(bodyHeight);
        accessoryList.initGui(left + PREVIEW_WIDTH + GAP, bodyTop);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(GuiSettingsWindow.BACKGROUND_TEXTURE);
        // Container
        Draw.borderBox(x, y, EDITOR_WIDTH, editorHeight, 4, 36, 126);
        // Title background
        Draw.texturedModalRect(x - 4, y - 17, 101, 0, 4, 16);
        Draw.texturedModalRect(x, y - 17, EDITOR_WIDTH - 16, 16, 105, 0, 1, 16);
        Draw.texturedModalRect(x + EDITOR_WIDTH - 21, y - 17, 106, 0, 19, 16);
        fontRenderer.drawStringWithShadow(I18n.format("mobends.gui.supporters"), x + 2, y - 13, 0xffffff);

        // On the light container, dark text as in the game's own windows.
        final int headerColor = isSupporter() ? 0x2e7d32 : 0x404040;
        int lineY = y + PADDING + 1;
        for (String line : headerLines)
        {
            fontRenderer.drawString(line, x + PADDING, lineY, headerColor);
            lineY += fontRenderer.FONT_HEIGHT;
        }

        // drawScreen's partialTicks is the frame's length in ticks (Minecraft#getTickLength), not the
        // partial tick the animators run at: the preview is drawn at the latter.
        drawPreview(DataUpdateHandler.partialTicks);
        accessoryList.draw(DataUpdateHandler.partialTicks);
        if (accessoryList.getListElements().isEmpty())
        {
            final String empty = I18n.format("mobends.gui.supporters.empty");
            final int listLeft = x + PADDING + PREVIEW_WIDTH + GAP;
            int emptyY = bodyTop + 10;
            for (String line : fontRenderer.listFormattedStringToWidth(empty, accessoryList.getWidth() - 16))
            {
                fontRenderer.drawString(line, listLeft + 8, emptyY, 0x9a9a9a);
                emptyY += fontRenderer.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (accessoryList.isLockHovered())
        {
            drawHoveringText(I18n.format("mobends.gui.supporters.locked"), mouseX, mouseY);
            // It leaves the item lighting on.
            RenderHelper.disableStandardItemLighting();
        }
    }

    /**
     * The player wearing the accessory picked, in a dark frame left of the list.
     */
    private void drawPreview(float partialTicks)
    {
        final int left = x + PADDING;
        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(GuiBenderList.LIST_TEXTURE);
        Draw.borderBox(left, bodyTop, PREVIEW_WIDTH, bodyHeight, 4, 36, 117);

        final int innerLeft = left + 3, innerTop = bodyTop + 3;
        final int innerWidth = PREVIEW_WIDTH - 6, innerHeight = bodyHeight - 6;
        GlStateManager.disableTexture2D();
        Draw.rectangleVerticalGradient(innerLeft, innerTop, innerWidth, innerHeight, 0xff2c2c2c, 0xff171717);
        GlStateManager.enableTexture2D();

        final EntityPreview preview = selectedKey == null ? null : previews.get(selectedKey);
        if (preview == null)
        {
            return;
        }

        UIScissorHelper.INSTANCE.push(innerLeft, innerTop, innerWidth, innerHeight);
        GlStateManager.pushMatrix();
        GlStateManager.translate(innerLeft + innerWidth / 2F, innerTop + innerHeight / 2F, 0F);
        preview.draw(Math.round((innerHeight - 12) * PREVIEW_SCALE), partialTicks);
        GlStateManager.popMatrix();
        UIScissorHelper.INSTANCE.pop();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        // The catalogue and what the player has arrive after the window opens, and anew after a refresh.
        if (fetchOwnSettings() != ownSettings || SupporterContent.getAccessoryDetailsRevision() != accessoryDetailsRevision)
        {
            fetchAccessories();
            initGui();
        }

        accessoryList.update(mouseX, mouseY);
        animatePreview();
    }

    /**
     * Loops the previewed player: it stands (or, for the sword trail, swings a combo), then turns
     * half around, eased in and out. Body, head and facing turn together, so the animators see no
     * turning of the head.
     */
    private void animatePreview()
    {
        final EntityPreview preview = selectedKey == null ? null : previews.get(selectedKey);
        if (preview == null)
        {
            return;
        }
        if (!selectedKey.equals(animatedKey))
        {
            animatedKey = selectedKey;
            animatedTicks = 0;
        }
        preview.tick(false);
        final EntityLivingBase entity = preview.getEntity();
        if (entity == null)
        {
            return;
        }

        final boolean swinging = SWORD_TRAIL_KEY.equals(selectedKey);
        final int turnStart = swinging ? COMBO_SWINGS * SWING_TICKS : STAND_TICKS;
        final int loopTicks = turnStart + TURN_TICKS + (swinging ? SETTLE_TICKS : 0);
        final int tick = animatedTicks++ % loopTicks;
        if (swinging && tick < turnStart && tick % SWING_TICKS == 0)
        {
            entity.swingArm(EnumHand.MAIN_HAND);
        }

        final float turn = turnedBy(tick + 1, turnStart) - turnedBy(tick, turnStart);
        entity.prevRotationYaw = entity.rotationYaw;
        entity.prevRenderYawOffset = entity.renderYawOffset;
        entity.prevRotationYawHead = entity.rotationYawHead;
        entity.rotationYaw += turn;
        entity.renderYawOffset += turn;
        entity.rotationYawHead += turn;
    }

    /** Degrees the previewed player has turned {@code tick} ticks into its loop, the turn starting at {@code turnStart}. */
    private static float turnedBy(int tick, int turnStart)
    {
        final float t = MathHelper.clamp((tick - turnStart) / (float) TURN_TICKS, 0F, 1F);
        return 180F * t * t * (3F - 2F * t);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        accessoryList.handleMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        accessoryList.handleMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        accessoryList.handleMouseInput();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        switch (keyCode)
        {
            case 1:
                GuiHelper.closeGui();
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);

        switch (button.id)
        {
            case COMPONENT_BUTTON_BACK:
                this.mc.displayGuiScreen(new GuiBendsMenu());
                break;
            case COMPONENT_BUTTON_SUPPORT:
                WebLink.SUPPORT.open();
                break;
            case COMPONENT_BUTTON_REFRESH:
                SupporterContent.fetchAccessoryDetails();
                if (mc.player != null)
                    SupporterContent.forgetPlayer(mc.player.getName());
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        for (EntityLivingBase entity : dressed)
            SupporterContent.setPreviewAccessories(entity, null);
        dressed.clear();
        if (previewWorld != null)
            previewWorld.close();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private boolean isSupporter()
    {
        return ownSettings != null && ownSettings.values().stream().anyMatch(AccessorySettings::isUnlocked);
    }

    /** Thanks supporters; tells everyone else what supporting gets them. */
    private String headerMessage()
    {
        return I18n.format(isSupporter() ? "mobends.gui.supporters.thanks" : "mobends.gui.supporters.pitch");
    }

    /** What the player has; an empty map until it's fetched (asking for it if it isn't). */
    private static Map<String, AccessorySettings> fetchOwnSettings()
    {
        // Not this.mc: the constructor asks too, and the game only sets mc after it.
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player == null ? Collections.emptyMap() : SupporterContent.getAccessorySettingsMapFor(player);
    }

    /** Makes a row for every accessory, by name, showing what the player has of it. */
    private void fetchAccessories()
    {
        ownSettings = fetchOwnSettings();
        accessoryDetailsRevision = SupporterContent.getAccessoryDetailsRevision();
        accessoryList.clearElements();
        accessories.clear();
        if (playerBender == null)
        {
            Core.LOG.warning("Unable to preview the supporter accessories: players have no bender.");
            return;
        }

        final List<Map.Entry<String, AccessoryDetails>> sorted = new ArrayList<>(SupporterContent.getAccessories());
        sorted.sort(Comparator.comparing(entry -> entry.getValue().getDisplayName(), String.CASE_INSENSITIVE_ORDER));
        for (Map.Entry<String, AccessoryDetails> entry : sorted)
        {
            final String key = entry.getKey();
            accessories.put(key, entry.getValue());
            final EntityPreview preview = previews.computeIfAbsent(key, k -> new EntityPreview(previewWorld, playerBender, made -> {
                dress(made, key);
                dressed.add(made);
                if (SWORD_TRAIL_KEY.equals(key) && !(made.getHeldItemMainhand().getItem() instanceof ItemSword))
                    made.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            }));
            if (preview.getEntity() != null)
                dress(preview.getEntity(), key);
            accessoryList.addElement(new GuiAccessoryRow(accessoryList, entry.getValue().getDisplayName(),
                    ownSettings.getOrDefault(key, AccessorySettings.DEFAULT), () -> key.equals(selectedKey), () -> selectedKey = key));
        }
        if (selectedKey == null || !accessories.containsKey(selectedKey))
            selectedKey = sorted.isEmpty() ? null : sorted.get(0).getKey();
    }

    /**
     * Puts the accessory on a preview, whether the player has it or not: in their color if they do,
     * else in the accessory's default color.
     */
    private void dress(EntityLivingBase entity, String key)
    {
        final AccessorySettings own = ownSettings == null ? null : ownSettings.get(key);
        final AccessoryDetails details = accessories.get(key);
        final IColorRead color = own != null && own.isUnlocked() ? own.getColor()
                : details != null ? details.getDefaultColor() : Color.WHITE;
        SupporterContent.setPreviewAccessories(entity, Collections.singletonMap(key, new AccessorySettings(true, false, color)));
    }

    @Nullable
    private static PreviewWorld makePreviewWorld()
    {
        try
        {
            return new PreviewWorld();
        }
        catch (Throwable e)
        {
            Core.LOG.warning("Unable to make the world of the accessory previews: " + e);
            return null;
        }
    }

}
