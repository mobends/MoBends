package goblinbob.mobends.core.client.gui.supporterswindow;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.Screens;
import goblinbob.mobends.core.client.gui.WebLink;
import goblinbob.mobends.core.client.gui.settingswindow.GuiBenderList;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.client.preview.EntityPreview;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import goblinbob.mobends.core.supporters.AccessoryDetails;
import goblinbob.mobends.core.supporters.AccessorySettings;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >=26.1 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Supporters window: on the left, the player wearing the accessory picked from the list on the
 * right (every supporter accessory, whether they have it or not), turning half around now and then
 * so it shows from both sides; for the sword trail, swinging a sword in combos. Above, a thank-you
 * to supporters or what supporting gets you; below, the link to support, and a refresh of what the
 * player has (for right after they've become a supporter). Logging in is on the Link account screen
 * of the Mo' Bends menu.
 */
public class GuiSupportersWindow extends Screen {
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
    private static final String SWORD_TRAIL_KEY = SupporterContent.SWORD_TRAIL;
    private static final int COMBO_SWINGS = 5;
    private static final int SWING_TICKS = 12;
    /**
     * Ticks the sword's previewed player stands after it turns. The player's animator starts the
     * combo over once 20 ticks have passed since the last swing, so the next one begins with its first.
     */
    private static final int SETTLE_TICKS = 8;

    /** The container's top left; its content starts PADDING inside. */
    private int x;
    private int y;
    private int editorHeight;
    private List<FormattedCharSequence> headerLines = Collections.emptyList();
    private int bodyTop;
    private int bodyHeight;
    private int mouseX;
    private int mouseY;

    private final GuiAccessoryList accessoryList = new GuiAccessoryList(0, 0, INNER_WIDTH - PREVIEW_WIDTH - GAP, 100);

    /** Where the previewed players stand, closed with the window. */
    private final PreviewWorld previewWorld = new PreviewWorld();
    /** A player for each accessory (by its key) dressed in it, kept while the rows are made anew. */
    private final Map<String, EntityPreview> previews = new HashMap<>();
    private final List<LivingEntity> dressed = new ArrayList<>();
    @Nullable
    private final DefinedEntity playerModel = EntityTypeRegistry.INSTANCE.defaultModelFor(Ids.minecraft("player"), RemotePlayer.class);
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
    /** Where leaving the Mo' Bends menus goes: the game (null), or the mod list it was opened from. */
    @Nullable
    private final Screen exit;

    public GuiSupportersWindow(@Nullable Screen exit) {
        super(Component.translatable("mobends.gui.supporters"));
        this.exit = exit;

        fetchAccessories();
        if (SupporterContent.getAccessories().isEmpty()) {
            SupporterContent.fetchAccessoryDetails();
        }
    }

    @Override
    protected void init() {
        this.editorHeight = Math.min(MAX_EDITOR_HEIGHT, this.height - 2 * SCREEN_MARGIN);
        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - editorHeight) / 2;
        int left = x + PADDING;

        // The container is drawn from (x, y), its content inset by the padding.
        this.headerLines = font.split(headerMessage(), INNER_WIDTH);
        if (headerLines.size() > MAX_HEADER_LINES) {
            headerLines = headerLines.subList(0, MAX_HEADER_LINES);
        }
        // The frames' borders are drawn outside the body, over the gaps around it: they're a GAP wider.
        this.bodyTop = y + PADDING + headerLines.size() * font.lineHeight + 2 * GAP;
        int buttonsY = y + editorHeight - PADDING - BUTTON_HEIGHT;
        this.bodyHeight = buttonsY - 2 * GAP - bodyTop;

        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> Screens.open(new GuiBendsMenu(exit)))
                .bounds(10, height - 30, 60, 20).build());
        Component refresh = Component.translatable("mobends.gui.supporters.refresh");
        int refreshWidth = font.width(refresh) + 20;
        int supportWidth = INNER_WIDTH - refreshWidth - GAP;
        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.supporters.support"), button -> WebLink.SUPPORT.open())
                .bounds(left, buttonsY, supportWidth, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(refresh, button -> refresh())
                .bounds(left + INNER_WIDTH - refreshWidth, buttonsY, refreshWidth, BUTTON_HEIGHT).build());

        accessoryList.setHeight(bodyHeight);
        accessoryList.initGui(left + PREVIEW_WIDTH + GAP, bodyTop);
    }

    /** Fetches the catalogue and what the player has again; the rows are made anew as they arrive. */
    private void refresh() {
        SupporterContent.fetchAccessoryDetails();
        LocalPlayer player = this.minecraft.player;
        if (player != null) {
            //? if >=26.1 {
            /*SupporterContent.forgetPlayer(player.getGameProfile().name());
            *///?} else {
            SupporterContent.forgetPlayer(player.getGameProfile().getName());
            //?}
        }
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

        if (accessoryList.isLockHovered()) {
            //? if >=26.1 {
            /*graphics.setTooltipForNextFrame(font, Component.translatable("mobends.gui.supporters.locked"), mouseX, mouseY);
            *///?} else {
            setTooltipForNextRenderPass(Component.translatable("mobends.gui.supporters.locked"));
            //?}
        }
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

        // The container at (x, y), its title on a tab above it.
        GuiSettingsWindow.drawWindow(graphics, x - 4, y - 4, EDITOR_WIDTH, editorHeight);
        graphics.drawString(font, this.title, x + 2, y - 13, 0xFFFFFFFF);

        // On the light container, dark text as in the game's own windows.
        int headerColor = isSupporter() ? 0xFF2E7D32 : 0xFF404040;
        int lineY = y + PADDING + 1;
        for (FormattedCharSequence line : headerLines) {
            graphics.drawString(font, line, x + PADDING, lineY, headerColor, false);
            lineY += font.lineHeight;
        }

        // The render's partialTicks is the frame's length in ticks, not the partial tick the
        // animators run at: the preview is drawn at the latter.
        float animationPartialTicks = DataUpdateHandler.partialTicks;
        drawPreview(graphics, animationPartialTicks);
        accessoryList.draw(graphics, mouseX, mouseY, animationPartialTicks);
        // While the catalogue is on its way, the list is just empty.
        if (accessoryList.getListElements().isEmpty() && !SupporterContent.isFetchingAccessories()) {
            int listLeft = x + PADDING + PREVIEW_WIDTH + GAP;
            int emptyY = bodyTop + 10;
            for (FormattedCharSequence line : font.split(Component.translatable("mobends.gui.supporters.empty"), accessoryList.getWidth() - 16)) {
                graphics.drawString(font, line, listLeft + 8, emptyY, 0xFF9A9A9A, false);
                emptyY += font.lineHeight;
            }
        }
    }

    /** The player wearing the accessory picked, in a dark frame left of the list. */
    private void drawPreview(GuiGraphics graphics, float partialTicks) {
        int left = x + PADDING;
        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, left, bodyTop, PREVIEW_WIDTH, bodyHeight, 4, 36, 117);

        int innerLeft = left + 3;
        int innerTop = bodyTop + 3;
        int innerWidth = PREVIEW_WIDTH - 6;
        int innerHeight = bodyHeight - 6;
        Draw.rectangleVerticalGradient(graphics, innerLeft, innerTop, innerWidth, innerHeight, 0xff2c2c2c, 0xff171717);

        EntityPreview preview = selectedKey == null ? null : previews.get(selectedKey);
        if (preview == null) {
            return;
        }

        graphics.enableScissor(innerLeft, innerTop, innerLeft + innerWidth, innerTop + innerHeight);
        preview.draw(graphics, innerLeft, innerTop, innerLeft + innerWidth, innerTop + innerHeight,
                Math.round((innerHeight - 12) * PREVIEW_SCALE), partialTicks);
        graphics.disableScissor();
    }

    @Override
    public void tick() {
        super.tick();

        // The catalogue and what the player has arrive after the window opens, and anew after a refresh.
        if (fetchOwnSettings() != ownSettings || SupporterContent.getAccessoryDetailsRevision() != accessoryDetailsRevision) {
            fetchAccessories();
            rebuildWidgets();
        }

        accessoryList.update(mouseX, mouseY);
        animatePreview();
    }

    /**
     * Loops the previewed player: it stands (or, for the sword trail, swings a combo), then turns
     * half around, eased in and out. Body, head and facing turn together, so the animators see no
     * turning of the head.
     */
    private void animatePreview() {
        EntityPreview preview = selectedKey == null ? null : previews.get(selectedKey);
        if (preview == null) {
            return;
        }
        if (!selectedKey.equals(animatedKey)) {
            animatedKey = selectedKey;
            animatedTicks = 0;
        }
        preview.tick(false);
        LivingEntity entity = preview.getEntity();
        if (entity == null) {
            return;
        }

        boolean swinging = SWORD_TRAIL_KEY.equals(selectedKey);
        int turnStart = swinging ? COMBO_SWINGS * SWING_TICKS : STAND_TICKS;
        int loopTicks = turnStart + TURN_TICKS + (swinging ? SETTLE_TICKS : 0);
        int tick = animatedTicks++ % loopTicks;
        if (swinging && tick < turnStart && tick % SWING_TICKS == 0) {
            preview.swing();
        }

        float turn = turnedBy(tick + 1, turnStart) - turnedBy(tick, turnStart);
        entity.yRotO = entity.getYRot();
        entity.yBodyRotO = entity.yBodyRot;
        entity.yHeadRotO = entity.yHeadRot;
        entity.setYRot(entity.getYRot() + turn);
        entity.yBodyRot += turn;
        entity.yHeadRot += turn;
    }

    /** Degrees the previewed player has turned {@code tick} ticks into its loop, the turn starting at {@code turnStart}. */
    private static float turnedBy(int tick, int turnStart) {
        float t = Mth.clamp((tick - turnStart) / (float) TURN_TICKS, 0F, 1F);
        return 180F * t * t * (3F - 2F * t);
    }

    //? if >=26.1 {
    /*@Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick) || accessoryList.handleMouseClicked(event.x(), event.y(), event.button());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        accessoryList.handleMouseReleased(event.x(), event.y(), event.button());
        return super.mouseReleased(event);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || accessoryList.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        accessoryList.handleMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }
    //?}

    //? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return accessoryList.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        return accessoryList.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    @Override
    public void onClose() {
        Screens.open(exit);
    }

    @Override
    public void removed() {
        for (LivingEntity entity : dressed) {
            SupporterContent.setPreviewAccessories(entity, null);
        }
        dressed.clear();
        previewWorld.close();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isSupporter() {
        return ownSettings != null && ownSettings.values().stream().anyMatch(AccessorySettings::unlocked);
    }

    /** Thanks supporters; tells everyone else what supporting gets them. */
    private Component headerMessage() {
        return Component.translatable(isSupporter() ? "mobends.gui.supporters.thanks" : "mobends.gui.supporters.pitch");
    }

    /** What the player has; an empty map until it's fetched (asking for it if it isn't). */
    private static Map<String, AccessorySettings> fetchOwnSettings() {
        // Not this.minecraft: the constructor asks too, and the game only sets it after.
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Collections.emptyMap() : SupporterContent.getAccessorySettingsMapFor(player);
    }

    /** Makes a row for every accessory, by name, showing what the player has of it. */
    private void fetchAccessories() {
        ownSettings = fetchOwnSettings();
        accessoryDetailsRevision = SupporterContent.getAccessoryDetailsRevision();
        accessoryList.clearElements();
        accessories.clear();
        if (playerModel == null) {
            MoBends.LOGGER.warn("Unable to preview the supporter accessories: players have no model.");
            return;
        }

        List<Map.Entry<String, AccessoryDetails>> sorted = new ArrayList<>(SupporterContent.getAccessories().entrySet());
        sorted.sort(Comparator.comparing(entry -> entry.getValue().displayName(), String.CASE_INSENSITIVE_ORDER));
        for (Map.Entry<String, AccessoryDetails> entry : sorted) {
            String key = entry.getKey();
            accessories.put(key, entry.getValue());
            EntityPreview preview = previews.computeIfAbsent(key, k -> new EntityPreview(previewWorld, playerModel, made -> {
                dress(made, key);
                dressed.add(made);
                if (SWORD_TRAIL_KEY.equals(key) && !made.getMainHandItem().is(ItemTags.SWORDS)) {
                    made.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
                }
            }));
            if (preview.getEntity() != null) {
                dress(preview.getEntity(), key);
            }
            accessoryList.addElement(new GuiAccessoryRow(accessoryList, entry.getValue().displayName(),
                    ownSettings.getOrDefault(key, AccessorySettings.DEFAULT), () -> key.equals(selectedKey), () -> selectedKey = key));
        }
        if (selectedKey == null || !accessories.containsKey(selectedKey)) {
            selectedKey = sorted.isEmpty() ? null : sorted.get(0).getKey();
        }
    }

    /**
     * Puts the accessory on a preview, whether the player has it or not: in their colour if they
     * do, else in the accessory's default colour.
     */
    private void dress(LivingEntity entity, String key) {
        AccessorySettings own = ownSettings == null ? null : ownSettings.get(key);
        AccessoryDetails details = accessories.get(key);
        int defaultColor = details != null ? details.defaultColorOrWhite() : 0xFFFFFF;
        int color = own != null && own.unlocked() ? own.colorOr(defaultColor) : defaultColor;
        SupporterContent.setPreviewAccessories(entity, Collections.singletonMap(key, new AccessorySettings(true, false, color)));
    }
}
