package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.kumo.option.KumoOption;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * An option of the Options window: its label, where it comes from and its default, and a switch
 * (a boolean) or buttons that step it down and up (a number).
 */
public class GuiOptionEntry implements IGuiListElement {
    private static final int BUTTON_SIZE = 20;
    /** Where the controls end, as in the order list. */
    private static final int RIGHT = 252;
    /** A number without a step moves by this part of its range. */
    private static final int STEPS_WITHOUT_STEP = 20;

    private final KumoOption option;
    private final String source;
    @Nullable
    private final GuiSmallToggleButton toggle;
    @Nullable
    private final GuiCustomButton downButton;
    @Nullable
    private final GuiCustomButton upButton;

    private int x;
    private int y;
    private int listOrder;

    /**
     * @param source where the option comes from, for its second line: the model definition, a type
     *               or an extension
     */
    public GuiOptionEntry(KumoOption option, String source) {
        this.option = option;
        this.source = source;
        if (option.isBoolean()) {
            this.toggle = new GuiSmallToggleButton();
            this.downButton = null;
            this.upButton = null;
        } else {
            this.toggle = null;
            this.downButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, Component.literal("-"));
            this.upButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, Component.literal("+"));
        }
    }

    /** The label the option's file gives it, translated, or else its name. */
    public static String labelOf(KumoOption option) {
        String label = option.getLabel();
        return label != null && Language.getInstance().has(label) ? I18n.get(label) : label != null ? label : option.getName();
    }

    /** A value as the menu shows it: on or off, or the number without trailing zeros. */
    public static String format(KumoOption option, double value) {
        if (option.isBoolean()) {
            return I18n.get(value != 0 ? "mobends.gui.options.on" : "mobends.gui.options.off");
        }
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        String text = String.format(Locale.ROOT, "%.3f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private double step() {
        return option.getStep() > 0 ? option.getStep() : (option.getMax() - option.getMin()) / STEPS_WITHOUT_STEP;
    }

    @Override
    public void initGui(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void layOut(int originX, int originY) {
        int left = originX + x;
        int top = originY + y;
        if (toggle != null) {
            toggle.initGui(left + RIGHT - 30, top + 8);
        } else {
            upButton.setPosition(left + RIGHT - BUTTON_SIZE, top + 5);
            downButton.setPosition(left + RIGHT - 3 * BUTTON_SIZE - 4, top + 5);
        }
    }

    @Override
    public boolean handleMouseClicked(int originX, int originY, double mouseX, double mouseY, int button) {
        layOut(originX, originY);
        if (toggle != null) {
            if (toggle.mousePressed(mouseX, mouseY, button)) {
                option.set(toggle.getToggleState() ? 1 : 0);
                return true;
            }
            return false;
        }
        if (downButton.mousePressed(mouseX, mouseY, button)) {
            option.set(option.get() - step());
            return true;
        }
        if (upButton.mousePressed(mouseX, mouseY, button)) {
            option.set(option.get() + step());
            return true;
        }
        return false;
    }

    @Override
    public void update(int mouseX, int mouseY) {
    }

    @Override
    public void draw(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        layOut(originX, originY);
        Font font = Minecraft.getInstance().font;
        int left = originX + x;
        int top = originY + y;

        int textWidth = RIGHT - 3 * BUTTON_SIZE - 12;
        String details = source + " · " + I18n.get("mobends.gui.options.default", format(option, option.getDefault()));
        graphics.drawString(font, font.plainSubstrByWidth(labelOf(option), textWidth), left + 6, top + 6, option.isDefault() ? 0xFFFFFFFF : 0xFFFFE680);
        graphics.drawString(font, font.plainSubstrByWidth(details, textWidth), left + 6, top + 18, 0xFF9A9A9A, false);

        if (toggle != null) {
            // Reset changes it from outside.
            toggle.setToggleState(option.isOn());
            Draw.widget(graphics, toggle, mouseX, mouseY, partialTicks);
        } else {
            downButton.active = option.get() > option.getMin();
            upButton.active = option.get() < option.getMax();
            String value = format(option, option.get());
            int center = left + RIGHT - BUTTON_SIZE - 2 - BUTTON_SIZE / 2;
            graphics.drawString(font, value, center - font.width(value) / 2, top + 11, 0xFFFFFFFF);
            Draw.widget(graphics, downButton, mouseX, mouseY, partialTicks);
            Draw.widget(graphics, upButton, mouseX, mouseY, partialTicks);
        }
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
        return 30;
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
