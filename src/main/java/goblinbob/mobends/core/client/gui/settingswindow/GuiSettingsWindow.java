package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.definition.DefinedEntities.DefinedEntity;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.preview.EntityPreview;
import goblinbob.mobends.core.client.preview.PreviewWorld;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The Settings window of the Mo' Bends menu: every animated mob, with its preview, whether it's
 * animated, and the buttons that open the order of its types and extensions and its options;
 * searchable by name. More Settings opens the rest of the client's settings ({@link GuiMoreSettings}).
 * Every change is saved at once ({@code ClientConfig}).
 */
public class GuiSettingsWindow extends Screen {
    public static final ResourceLocation BACKGROUND_TEXTURE = Ids.of(MoBends.MOD_ID, "textures/gui/settings_window.png");
    public static final int EDITOR_WIDTH = 280;
    /** The least height; the window grows with the screen up to the max, clear of its edges and the back button. */
    public static final int EDITOR_HEIGHT = 177;
    private static final int MAX_EDITOR_HEIGHT = 400;
    private static final int SCREEN_MARGIN = 40;

    private int x;
    private int y;
    private int editorHeight = EDITOR_HEIGHT;
    private int mouseX;
    private int mouseY;

    private EditBox filterQueryInput;
    private final GuiBenderList bendsSettingsListUI = new GuiBenderList(0, 0, EDITOR_WIDTH - 10, EDITOR_HEIGHT - 10 - 20);

    /** Where the mobs of the rows stand, closed with the window. */
    private final PreviewWorld previewWorld = new PreviewWorld();
    /** Each mob's preview, kept while searching. */
    private final Map<DefinedEntity, EntityPreview> previews = new HashMap<>();

    private String query = "";
    /** Where leaving the Mo' Bends menus goes: the game (null), or the mod list it was opened from. */
    @Nullable
    private final Screen exit;

    public GuiSettingsWindow(@Nullable Screen exit) {
        super(Component.translatable("mobends.gui.settings"));
        this.exit = exit;
        fetchBenders();
    }

    @Override
    protected void init() {
        this.editorHeight = Math.max(EDITOR_HEIGHT, Math.min(MAX_EDITOR_HEIGHT, this.height - 2 * SCREEN_MARGIN));
        this.x = (this.width - EDITOR_WIDTH) / 2;
        this.y = (this.height - editorHeight) / 2;

        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> goBack())
                .bounds(10, height - 30, 60, 20).build());
        // The rest of the client's settings (1.12's Forge config screen).
        Component moreSettings = Component.translatable("mobends.gui.more_settings");
        int moreSettingsWidth = this.font.width(moreSettings) + 20;
        addRenderableWidget(Button.builder(moreSettings, button -> this.minecraft.setScreen(new GuiMoreSettings(new GuiSettingsWindow(exit), exit)))
                .bounds(width - 10 - moreSettingsWidth, height - 30, moreSettingsWidth, 20).build());

        String previousQuery = filterQueryInput != null ? filterQueryInput.getValue() : query;
        filterQueryInput = new EditBox(this.font, x + 6, y + 6, 150, 16, Component.translatable("mobends.gui.search"));
        filterQueryInput.setHint(Component.translatable("mobends.gui.search"));
        filterQueryInput.setValue(previousQuery);
        filterQueryInput.setResponder(text -> {
            if (!text.equals(query)) {
                query = text;
                fetchBenders();
            }
        });
        addRenderableWidget(filterQueryInput);
        setInitialFocus(filterQueryInput);

        bendsSettingsListUI.setHeight(editorHeight - 10 - 20);
        bendsSettingsListUI.initGui(this.x + 9, this.y + 9 + 20);
    }

    /** The frame of the Mo' Bends menu's windows: the container, and the tab its title goes on. */
    public static void drawWindow(GuiGraphics graphics, int x, int y, int width, int height) {
        // Container
        Draw.borderBox(graphics, BACKGROUND_TEXTURE, x + 4, y + 4, width, height, 4, 36, 126);
        // Title background
        Draw.texturedModalRect(graphics, BACKGROUND_TEXTURE, x, y - 13, 101, 0, 4, 16);
        Draw.texturedModalRect(graphics, BACKGROUND_TEXTURE, x + 4, y - 13, width - 16, 16, 105, 0, 1, 16);
        Draw.texturedModalRect(graphics, BACKGROUND_TEXTURE, x + width - 17, y - 13, 106, 0, 19, 16);
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

        drawWindow(graphics, x, y, EDITOR_WIDTH, editorHeight);
        bendsSettingsListUI.draw(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(this.font, this.title, this.x + 6, this.y - 9, 0xffffff);
    }

    @Override
    public void tick() {
        super.tick();
        bendsSettingsListUI.update(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return bendsSettingsListUI.handleMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        bendsSettingsListUI.handleMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    //? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return bendsSettingsListUI.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        return bendsSettingsListUI.handleMouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollY);
    }
    *///?}

    private void goBack() {
        this.minecraft.setScreen(new GuiBendsMenu(exit));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(exit);
    }

    @Override
    public void removed() {
        previewWorld.close();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** The mobs whose name holds the query, by key. */
    private List<DefinedEntity> search() {
        List<DefinedEntity> found = new ArrayList<>(EntityTypeRegistry.INSTANCE.getDefinedEntities());
        String lowerCase = query.toLowerCase(Locale.ROOT);
        if (!lowerCase.isEmpty()) {
            found.removeIf(defined -> !GuiBenderSettings.nameOf(defined).getString().toLowerCase(Locale.ROOT).contains(lowerCase));
        }
        found.sort(Comparator.comparing(DefinedEntity::key));
        return found;
    }

    public void fetchBenders() {
        bendsSettingsListUI.clearElements();
        for (DefinedEntity defined : search()) {
            int typeCount = EntityTypeRegistry.INSTANCE.getTypesFor(defined).size();
            int extensionCount = EntityTypeRegistry.INSTANCE.getExtensionsFor(defined).size();
            int optionCount = GuiOptionsWindow.optionsOf(defined).size();
            EntityPreview preview = previews.computeIfAbsent(defined, d -> new EntityPreview(previewWorld, d));
            bendsSettingsListUI.addElement(new GuiBenderSettings(bendsSettingsListUI, defined, preview,
                    typeCount, () -> this.minecraft.setScreen(GuiOrderWindow.forTypes(defined, exit)),
                    extensionCount, () -> this.minecraft.setScreen(GuiOrderWindow.forExtensions(defined, exit)),
                    optionCount, () -> this.minecraft.setScreen(new GuiOptionsWindow(defined, exit))));
        }
    }
}
