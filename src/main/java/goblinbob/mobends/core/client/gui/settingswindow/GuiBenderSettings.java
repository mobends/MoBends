package goblinbob.mobends.core.client.gui.settingswindow;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.PackTrust;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.client.preview.EntityPreview;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * A row of the Settings window: the mob animated in its frame, its name and where it comes from,
 * the toggle that animates it, the buttons that order its types and extensions, and the one that
 * opens its options. The mob walks while the row is hovered, and the row is dimmed while the mob
 * isn't animated.
 */
public class GuiBenderSettings implements IGuiListElement {
    private static final int HEIGHT = 40;
    private static final int FRAME_SIZE = 36;
    /** Inside the frame's 2px border, what the mob is clipped to. */
    private static final int THUMBNAIL_SIZE = FRAME_SIZE - 4;
    /** The hitbox fits this, leaving a little room around it. */
    private static final int MOB_SIZE = THUMBNAIL_SIZE - 4;
    private static final int TEXT_X = FRAME_SIZE + 6;
    private static final int BUTTON_HEIGHT = 14;

    private final GuiBenderList list;
    private final DefinedEntity defined;
    private final String key;
    private final EntityPreview preview;
    private final String name;
    private final String source;
    private final Minecraft mc;
    private final GuiSmallToggleButton toggleButton;
    /** Opens the order of the entity's types; only there when more than one can animate it. */
    @Nullable
    private final GuiCustomButton orderButton;
    private final Runnable onOpenOrder;
    /** Opens the order of the extensions of those types; only there when they have more than one. */
    @Nullable
    private final GuiCustomButton extensionsButton;
    private final Runnable onOpenExtensions;
    /** Opens its options; only there when it has any. */
    @Nullable
    private final GuiCustomButton optionsButton;
    private final Runnable onOpenOptions;

    private int x;
    private int y;
    private int width;
    private boolean hovered;
    private int listOrder;

    public GuiBenderSettings(GuiBenderList list, DefinedEntity defined, EntityPreview preview,
                             int typeCount, Runnable onOpenOrder, int extensionCount, Runnable onOpenExtensions,
                             int optionCount, Runnable onOpenOptions) {
        this.list = list;
        this.defined = defined;
        this.key = defined.key();
        this.preview = preview;
        this.name = nameOf(defined).getString();
        this.source = sourceOf(defined);
        this.mc = Minecraft.getInstance();
        this.toggleButton = new GuiSmallToggleButton();
        this.toggleButton.setToggleState(ClientConfig.INSTANCE.isAnimated(key));
        this.orderButton = typeCount > 1 ? compactButton(Component.translatable("mobends.gui.typeorder.button", typeCount)) : null;
        this.onOpenOrder = onOpenOrder;
        this.extensionsButton = extensionCount > 1 ? compactButton(Component.translatable("mobends.gui.extensionorder.button", extensionCount)) : null;
        this.onOpenExtensions = onOpenExtensions;
        this.optionsButton = optionCount > 0 ? compactButton(Component.translatable("mobends.gui.options.button", optionCount)) : null;
        this.onOpenOptions = onOpenOptions;
    }

    private static GuiCustomButton compactButton(Component text) {
        // An even width, so the button's halves meet.
        int textWidth = Minecraft.getInstance().font.width(text);
        return new GuiCustomButton((textWidth + 11) & ~1, BUTTON_HEIGHT, text);
    }

    /** The mob's name: its entity type's, or else its key. */
    public static Component nameOf(DefinedEntity defined) {
        ResourceLocation id = defined.entityTypeId();
        if (id != null) {
            Optional<net.minecraft.world.entity.EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
            if (type.isPresent()) {
                return type.get().getDescription();
            }
        }
        return Component.literal(String.valueOf(defined.key()));
    }

    @Override
    public void initGui(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = list.getRowWidth();
    }

    /** Puts the widgets where the row is on the screen, its content starting at {@code originX}, {@code originY}. */
    private void layOut(int originX, int originY) {
        int rowX = originX + x;
        int rowY = originY + y;
        // Raised when the row has buttons, which can reach under it.
        this.toggleButton.initGui(rowX + width - 34, rowY + (hasButtons() ? 3 : 10));
        int buttonX = rowX + TEXT_X;
        if (orderButton != null) {
            this.orderButton.setPosition(buttonX, rowY + 23);
            buttonX += orderButton.getWidth() + 3;
        }
        if (extensionsButton != null) {
            this.extensionsButton.setPosition(buttonX, rowY + 23);
            buttonX += extensionsButton.getWidth() + 3;
        }
        if (optionsButton != null) {
            this.optionsButton.setPosition(buttonX, rowY + 23);
        }
    }

    private boolean hasButtons() {
        return orderButton != null || extensionsButton != null || optionsButton != null;
    }

    @Override
    public boolean handleMouseClicked(int originX, int originY, double mouseX, double mouseY, int button) {
        layOut(originX, originY);
        if (toggleButton.mousePressed(mouseX, mouseY, button)) {
            ClientConfig.INSTANCE.setAnimated(key, toggleButton.getToggleState());
            return true;
        }
        if (orderButton != null && orderButton.mousePressed(mouseX, mouseY, button)) {
            onOpenOrder.run();
            return true;
        }
        if (extensionsButton != null && extensionsButton.mousePressed(mouseX, mouseY, button)) {
            onOpenExtensions.run();
            return true;
        }
        if (optionsButton != null && optionsButton.mousePressed(mouseX, mouseY, button)) {
            onOpenOptions.run();
            return true;
        }
        return false;
    }

    @Override
    public void update(int mouseX, int mouseY) {
        this.hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + HEIGHT;
        preview.tick(hovered);
    }

    @Override
    public void draw(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        layOut(originX, originY);
        boolean animated = ClientConfig.INSTANCE.isAnimated(key);
        int rowX = originX + x;
        int rowY = originY + y;
        Font font = mc.font;

        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, rowX + 4, rowY + 4, width - 8, HEIGHT - 8, 4, hovered ? 140 : 130, 0);
        Draw.borderBox(graphics, GuiBenderList.LIST_TEXTURE, rowX + 4, rowY + 4, FRAME_SIZE - 4, FRAME_SIZE - 4, 2, 150, 0);

        drawThumbnail(graphics, rowX, rowY, animated, partialTicks);

        int textWidth = width - 34 - 4 - TEXT_X;
        int nameY = rowY + (hasButtons() ? 4 : 10);
        graphics.drawString(font, font.plainSubstrByWidth(name, textWidth), rowX + TEXT_X, nameY, animated ? 0xffffff : 0x8a8a8a);
        graphics.drawString(font, font.plainSubstrByWidth(source, textWidth), rowX + TEXT_X, nameY + 11, animated ? 0x9a9a9a : 0x626262, false);

        toggleButton.render(graphics, mouseX, mouseY, partialTicks);
        if (orderButton != null) {
            orderButton.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (extensionsButton != null) {
            extensionsButton.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (optionsButton != null) {
            optionsButton.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    private void drawThumbnail(GuiGraphics graphics, int rowX, int rowY, boolean animated, float partialTicks) {
        int left = rowX + 2 + 2;
        int top = rowY + 2 + 2;
        Draw.rectangleVerticalGradient(graphics, left, top, THUMBNAIL_SIZE, THUMBNAIL_SIZE, 0xff2c2c2c, 0xff171717);

        // Within the list's clip.
        graphics.enableScissor(left, top, left + THUMBNAIL_SIZE, top + THUMBNAIL_SIZE);
        // The animation's partial tick: what a screen is handed is the frame's real time in ticks.
        preview.draw(graphics, left + THUMBNAIL_SIZE / 2F, top + THUMBNAIL_SIZE / 2F, MOB_SIZE, DataUpdateHandler.partialTicks);
        if (!animated) {
            // Over the mob, which is drawn in front of the frame.
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            Draw.rectangle(graphics, left, top, THUMBNAIL_SIZE, THUMBNAIL_SIZE, 0x80000000);
            graphics.pose().popPose();
        }
        graphics.disableScissor();
    }

    /**
     * The mod that adds the mob, followed by the resource pack that animates it ("Minecraft · from
     * Some Pack"), or else by the mod that does ("Twilight Forest · via Some Addon") unless that is
     * the same mod, or Mo' Bends.
     */
    private static String sourceOf(DefinedEntity defined) {
        ResourceLocation entityId = defined.entityTypeId();
        String entityMod = entityId == null ? "minecraft" : entityId.getNamespace();
        String resourcePack = resourcePackOf(defined.location());
        if (resourcePack != null) {
            return Component.translatable("mobends.gui.settings.from_pack", modName(entityMod), resourcePack).getString();
        }
        String benderMod = Ids.parse(String.valueOf(defined.key())).getNamespace();
        if (benderMod.equals(MoBends.MOD_ID) || benderMod.equals(entityMod)) {
            return modName(entityMod);
        }
        return Component.translatable("mobends.gui.settings.via", modName(entityMod), modName(benderMod)).getString();
    }

    /** The resource pack the model definition at {@code location} is read from, or null if it's a mod's. */
    @Nullable
    private static String resourcePackOf(ResourceLocation location) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            return null;
        }
        String packId = resource.get().sourcePackId();
        if (PackTrust.isUserPack(packId)) {
            return packId.substring(packId.indexOf('/') + 1);
        }
        if (packId.startsWith("server")) {
            return Component.translatable("mobends.gui.settings.server_pack").getString();
        }
        return null;
    }

    private static String modName(String modId) {
        String name = MoBends.platform().modName(modId);
        if (name != null) {
            return name;
        }
        return "minecraft".equals(modId) ? "Minecraft" : modId;
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
