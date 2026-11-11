package goblinbob.mobends.core.client.gui;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.client.gui.elements.GuiSectionButton;
import goblinbob.mobends.core.client.gui.settingswindow.GuiSettingsWindow;
import goblinbob.mobends.core.client.gui.supporterswindow.GuiSupportersWindow;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.connection.MoBendsAccount;
import goblinbob.mobends.core.util.Ids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * The Mo' Bends menu: the title, with the Mo' Bends account's buttons under it, and the sections
 * (Settings, Customize, Supporters) beside it; Refresh in the corner. Opened in a world, by its key
 * (see {@code KeyboardHandler}) or from the mod list (the mod's config screen, {@link #fromModList}).
 */
public class GuiBendsMenu extends Screen {
    private static final ResourceLocation MENU_TITLE_TEXTURE = Ids.of(MoBends.MOD_ID, "textures/gui/title.png");
    public static final ResourceLocation ICONS_TEXTURE = Ids.of(MoBends.MOD_ID, "textures/gui/icons.png");
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    /** The title texture's size: its pixel art is drawn at twice the size of a pixel. */
    private static final int TITLE_TEXTURE_WIDTH = 334;
    private static final int TITLE_TEXTURE_HEIGHT = 74;
    /**
     * The title and the sections share three columns: the title, with the account's buttons under
     * it, takes up the first two, the sections, one above the other, the third.
     */
    private static final int MAX_CONTENT_WIDTH = 378;
    private static final int COLUMN_GAP = 6;
    private static final int SECTION_GAP = 2;
    private static final int SECTION_COUNT = 3;
    private static final int SCREEN_MARGIN = 10;

    private final GuiSectionButton settingsButton;
    private final GuiSectionButton customizeButton;
    private final GuiSectionButton supportersButton;
    /** Where leaving the Mo' Bends menus goes: the game (null), or the mod list it was opened from. */
    @Nullable
    private final Screen exit;

    public GuiBendsMenu() {
        this(null);
    }

    /**
     * What the mod list's Config button opens: the Mo' Bends menu, leaving back to {@code modList},
     * or, with no world loaded (the title screen), a notice to open it in one. Its previews and the
     * types of entity types need a level to make entities in, and a level of its own would need a
     * connection and tell other mods a level loaded.
     */
    public static Screen fromModList(Screen modList) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return new AlertScreen(() -> mc.setScreen(modList), Component.literal("Mo' Bends"),
                    Component.translatable("mobends.gui.needs_world", Component.keybind("key.mobends.menu")));
        }
        return new GuiBendsMenu(modList);
    }

    public GuiBendsMenu(@Nullable Screen exit) {
        super(Component.literal("Mo' Bends"));
        this.exit = exit;

        this.settingsButton = new GuiSectionButton(Component.translatable("mobends.gui.section.settings"), 0xFFDA3A00)
                .setIcon(120, 43, 14, 14);
        this.customizeButton = new GuiSectionButton(Component.translatable("mobends.gui.section.customize"), 0xFF26DAA3)
                .setIcon(80, 43, 19, 14);
        this.supportersButton = new GuiSectionButton(Component.translatable("mobends.gui.section.supporters"), 0xFFD8306A)
                .setIcon(100, 43, 18, 16);

        // The account may have been unlinked or renamed on the dashboard.
        MoBendsAccount.verify(() -> {
            if (Minecraft.getInstance().screen == this) {
                this.rebuildWidgets();
            }
        });
    }

    @Override
    protected void init() {
        Layout layout = new Layout();
        GuiSectionButton[] sections = {this.settingsButton, this.customizeButton, this.supportersButton};
        for (int i = 0; i < sections.length; ++i) {
            sections[i].initGui(layout.sectionsX, layout.sectionsY + i * (GuiSectionButton.HEIGHT + SECTION_GAP), layout.column);
        }
        addAccountButtons(layout);

        // Refresh reloads the animation (see CoreClient#reloadAnimation()) and refreshes what comes
        // from the Mo' Bends API (see ConnectionManager#refresh()).
        Component refresh = Component.translatable("mobends.gui.refresh");
        int refreshWidth = this.font.width(refresh) + 20;
        this.addRenderableWidget(Button.builder(refresh, button -> {
            CoreClient.reloadAnimation();
            ConnectionManager.refresh();
            // The account is read again.
            this.rebuildWidgets();
        }).bounds(10, this.height - 30, refreshWidth, BUTTON_HEIGHT).build());
    }

    /**
     * Under the title: Link account, half as wide and centered, or once linked, across its width,
     * the Mo' Bends account's name (opening the dashboard) and Log out.
     */
    private void addAccountButtons(Layout layout) {
        int x = layout.titleX;
        int y = layout.buttonsY;
        int width = layout.titleWidth;

        MoBendsAccount.Session session = MoBendsAccount.getCurrent();
        if (session == null) {
            int linkWidth = width / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("mobends.gui.link.button"),
                    button -> this.minecraft.setScreen(new GuiLinkAccount(this)))
                    .bounds(x + (width - linkWidth) / 2, y, linkWidth, BUTTON_HEIGHT).build());
            return;
        }

        Component logOut = Component.translatable("mobends.gui.link.log_out");
        int logOutWidth = this.font.width(logOut) + 20;
        int nameWidth = width - BUTTON_GAP - logOutWidth;
        this.addRenderableWidget(Button.builder(logOut, button -> {
            MoBendsAccount.logOut();
            this.rebuildWidgets();
        }).bounds(x + nameWidth + BUTTON_GAP, y, logOutWidth, BUTTON_HEIGHT).build());

        String name = session.displayName();
        int maxNameWidth = nameWidth - 8;
        if (this.font.width(name) > maxNameWidth) {
            name = this.font.plainSubstrByWidth(name, maxNameWidth - this.font.width("...")) + "...";
        }
        this.addRenderableWidget(Button.builder(Component.literal(name), button -> WebLink.ACCOUNT.open())
                .bounds(x, y, nameWidth, BUTTON_HEIGHT).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (settingsButton.mouseClicked(mouseX, mouseY, button)) {
            this.minecraft.setScreen(new GuiSettingsWindow(exit));
            return true;
        }
        if (customizeButton.mouseClicked(mouseX, mouseY, button)) {
            // Opens the link the static API names: meant to become the web animation editor (see TODO.md).
            WebLink.EDITOR.open();
            return true;
        }
        if (supportersButton.mouseClicked(mouseX, mouseY, button)) {
            this.minecraft.setScreen(new GuiSupportersWindow(exit));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(exit);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //? if <1.20.2 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTicks);

        Layout layout = new Layout();
        RenderSystem.enableBlend();
        graphics.blit(MENU_TITLE_TEXTURE, layout.titleX, layout.titleY, layout.titleWidth, layout.titleHeight, 0, 0,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT, TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        this.settingsButton.display(graphics, mouseX, mouseY);
        this.customizeButton.display(graphics, mouseX, mouseY);
        this.supportersButton.display(graphics, mouseX, mouseY);
    }

    /**
     * Where the title and the sections go: three columns, as wide as fit on the screen, centered a
     * little above the middle. The title, with its buttons under it, fills the first two and the
     * sections stack in the third, the bottoms of both lined up.
     */
    private class Layout {
        final int column;
        final int titleX;
        final int titleY;
        final int titleWidth;
        final int titleHeight;
        final int buttonsY;
        final int sectionsX;
        final int sectionsY;

        Layout() {
            int contentWidth = Math.min(MAX_CONTENT_WIDTH, width - 2 * SCREEN_MARGIN);
            column = (contentWidth - 2 * COLUMN_GAP) / 3;
            titleWidth = 2 * column + COLUMN_GAP;
            titleHeight = Math.round((float) titleWidth * TITLE_TEXTURE_HEIGHT / TITLE_TEXTURE_WIDTH);
            int titleColumnHeight = titleHeight + BUTTON_GAP + BUTTON_HEIGHT;
            int sectionsHeight = SECTION_COUNT * GuiSectionButton.HEIGHT + (SECTION_COUNT - 1) * SECTION_GAP;
            int contentHeight = Math.max(titleColumnHeight, sectionsHeight);
            int left = (width - (3 * column + 2 * COLUMN_GAP)) / 2;
            int top = (height - contentHeight) / 2 - 10;

            // Both columns end at the same line: the buttons under the title line up with the last section.
            int bottom = top + contentHeight;
            titleX = left;
            buttonsY = bottom - BUTTON_HEIGHT;
            titleY = buttonsY - BUTTON_GAP - titleHeight;
            sectionsX = left + titleWidth + COLUMN_GAP;
            sectionsY = bottom - sectionsHeight;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
//?}
