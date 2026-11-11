package goblinbob.mobends.core.client.gui.settingswindow;

// The Mo' Bends menu isn't ported to 26.x's GUI yet.
//? if <26.1 {
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.configuration.ClientConfig.PatternList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The rest of the client's settings ({@code config/mobends.json}, see docs/configuration.md): the
 * trails, and the lists of item and entity patterns, each written as one line, its entries
 * separated by commas. 1.12's More Settings opened Forge's config screen for them; this is the
 * screen's place on every loader. The trails change at once; the lists when the screen is left,
 * which reloads the animation if they changed (as 1.12 refreshed Mo' Bends).
 */
public class GuiMoreSettings extends Screen {
    private static final int COLUMN_WIDTH = 310;
    private static final int ROW_HEIGHT = 20;
    private static final int LABEL_HEIGHT = 11;
    private static final int LIST_GAP = 5;
    private static final int MAX_LENGTH = 4096;

    private static final String[] LIST_KEYS = {
            "mobends.config.item_use_classifications",
            "mobends.config.item_attack_classifications",
            "mobends.config.keep_armor_as_vanilla",
            "mobends.config.keep_entity_as_vanilla",
    };
    /** Only these lists have a tooltip. */
    private static final boolean[] LIST_TOOLTIPS = {true, true, false, false};

    /** Where Back goes: the Settings window. */
    private final Screen back;
    /** Where leaving the Mo' Bends menus goes: the game (null), or the mod list it was opened from. */
    @Nullable
    private final Screen exit;
    private final Map<PatternList, EditBox> lists = new EnumMap<>(PatternList.class);
    /** What the lists' boxes hold, kept across a resize (which makes the boxes anew). */
    private final Map<PatternList, String> typed = new EnumMap<>(PatternList.class);
    private int listsTop;

    public GuiMoreSettings(Screen back, @Nullable Screen exit) {
        super(Component.translatable("mobends.gui.more_settings"));
        this.back = back;
        this.exit = exit;
        for (PatternList list : PatternList.values()) {
            typed.put(list, String.join(", ", ClientConfig.INSTANCE.patterns(list)));
        }
    }

    @Override
    protected void init() {
        int left = (this.width - COLUMN_WIDTH) / 2;
        int halfWidth = (COLUMN_WIDTH - 10) / 2;
        int top = Math.max(30, (this.height - 30 - (ROW_HEIGHT + 10 + 4 * (LABEL_HEIGHT + ROW_HEIGHT + LIST_GAP))) / 2);

        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.INSTANCE.showSwordTrail())
                .withTooltip(value -> Tooltip.create(Component.translatable("mobends.config.show_sword_trails.tooltip")))
                .create(left, top, halfWidth, ROW_HEIGHT, Component.translatable("mobends.config.show_sword_trails"),
                        (button, value) -> ClientConfig.INSTANCE.setShowSwordTrail(value)));
        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.INSTANCE.showArrowTrails())
                .withTooltip(value -> Tooltip.create(Component.translatable("mobends.config.show_arrow_trails.tooltip")))
                .create(left + COLUMN_WIDTH - halfWidth, top, halfWidth, ROW_HEIGHT, Component.translatable("mobends.config.show_arrow_trails"),
                        (button, value) -> ClientConfig.INSTANCE.setShowArrowTrails(value)));

        listsTop = top + ROW_HEIGHT + 10;
        lists.clear();
        PatternList[] all = PatternList.values();
        for (int i = 0; i < all.length; i++) {
            PatternList list = all[i];
            int y = listsTop + i * (LABEL_HEIGHT + ROW_HEIGHT + LIST_GAP) + LABEL_HEIGHT;
            EditBox box = new EditBox(this.font, left, y, COLUMN_WIDTH, ROW_HEIGHT, Component.translatable(LIST_KEYS[i]));
            box.setMaxLength(MAX_LENGTH);
            box.setValue(typed.get(list));
            box.setResponder(text -> typed.put(list, text));
            if (LIST_TOOLTIPS[i]) {
                box.setTooltip(Tooltip.create(Component.translatable(LIST_KEYS[i] + ".tooltip")));
            }
            lists.put(list, addRenderableWidget(box));
        }

        addRenderableWidget(Button.builder(Component.translatable("mobends.gui.back"), button -> leave(back))
                .bounds(10, height - 30, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        //? if <1.20.2 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xffffff);
        int left = (this.width - COLUMN_WIDTH) / 2;
        for (int i = 0; i < LIST_KEYS.length; i++) {
            int y = listsTop + i * (LABEL_HEIGHT + ROW_HEIGHT + LIST_GAP);
            graphics.drawString(this.font, Component.translatable(LIST_KEYS[i]), left, y + 1, 0xa0a0a0);
        }
    }

    @Override
    public void onClose() {
        leave(exit);
    }

    /** Saves the lists that changed (reloading the animation if any did), and goes to {@code next}. */
    private void leave(@Nullable Screen next) {
        boolean changed = false;
        for (PatternList list : PatternList.values()) {
            List<String> entries = parse(typed.get(list));
            if (!entries.equals(ClientConfig.INSTANCE.patterns(list))) {
                ClientConfig.INSTANCE.setPatterns(list, entries);
                changed = true;
            }
        }
        if (changed) {
            CoreClient.reloadAnimation();
        }
        this.minecraft.setScreen(next);
    }

    /** The entries of a line: separated by commas, trimmed, the empty ones left out. */
    private static List<String> parse(String line) {
        List<String> entries = new ArrayList<>();
        for (String entry : line.split(",")) {
            String trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
//?}
