package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import goblinbob.mobends.core.kumo.option.KumoOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

/**
 * An option of the Options window: its label, where it comes from and its default, and a switch
 * (a boolean) or buttons that step it down and up (a number).
 */
public class GuiOptionEntry implements IGuiListElement
{

    private static final int BUTTON_SIZE = 20;
    /** Where the controls end, as in the order list. */
    private static final int RIGHT = 252;
    /** A number without a step moves by this part of its range. */
    private static final int STEPS_WITHOUT_STEP = 20;

    private final KumoOption option;
    private final String source;
    private final Minecraft mc;
    private final GuiSmallToggleButton toggle;
    private final GuiCustomButton downButton;
    private final GuiCustomButton upButton;

    private int x, y;
    private int mouseX, mouseY;
    private int listOrder;

    /**
     * @param source where the option comes from, for its second line: the model definition, a type
     *               or an extension
     */
    public GuiOptionEntry(KumoOption option, String source)
    {
        this.option = option;
        this.source = source;
        this.mc = Minecraft.getMinecraft();
        if (option.isBoolean())
        {
            this.toggle = new GuiSmallToggleButton();
            this.downButton = null;
            this.upButton = null;
        }
        else
        {
            this.toggle = null;
            this.downButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, "-");
            this.upButton = new GuiCustomButton(BUTTON_SIZE, BUTTON_SIZE, "+");
        }
    }

    /** The label the option's file gives it, translated, or else its name. */
    public static String labelOf(KumoOption option)
    {
        String label = option.getLabel();
        return label != null && I18n.hasKey(label) ? I18n.format(label) : label != null ? label : option.getName();
    }

    /** A value as the menu shows it: on or off, or the number without trailing zeros. */
    public static String format(KumoOption option, double value)
    {
        if (option.isBoolean())
        {
            return I18n.format(value != 0 ? "mobends.gui.options.on" : "mobends.gui.options.off");
        }
        if (value == Math.rint(value))
        {
            return Long.toString((long) value);
        }
        String text = String.format(java.util.Locale.ROOT, "%.3f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private double step()
    {
        return option.getStep() > 0 ? option.getStep() : (option.getMax() - option.getMin()) / STEPS_WITHOUT_STEP;
    }

    @Override
    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        if (toggle != null)
        {
            toggle.initGui(x + RIGHT - 30, y + 8);
        }
        else
        {
            upButton.setPosition(x + RIGHT - BUTTON_SIZE, y + 5);
            downButton.setPosition(x + RIGHT - 3 * BUTTON_SIZE - 4, y + 5);
        }
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        if (toggle != null)
        {
            if (toggle.mouseClicked(mouseX, mouseY, button))
            {
                option.set(toggle.getToggleState() ? 1 : 0);
                return true;
            }
            return false;
        }
        if (button != 0)
        {
            return false;
        }
        if (downButton.mousePressed(mouseX, mouseY))
        {
            option.set(option.get() - step());
            return true;
        }
        if (upButton.mousePressed(mouseX, mouseY))
        {
            option.set(option.get() + step());
            return true;
        }
        return false;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        if (toggle != null)
        {
            // Reset changes it from outside.
            toggle.setToggleState(option.isOn());
            toggle.update(mouseX, mouseY);
        }
        else
        {
            downButton.enabled = option.get() > option.getMin();
            upButton.enabled = option.get() < option.getMax();
        }
    }

    @Override
    public void draw(float partialTicks)
    {
        GlStateManager.color(1F, 1F, 1F);

        int textWidth = RIGHT - 3 * BUTTON_SIZE - 12;
        String details = source + " · " + I18n.format("mobends.gui.options.default", format(option, option.getDefault()));
        mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(labelOf(option), textWidth), x + 6, y + 6, option.isDefault() ? 0xffffff : 0xffe680);
        mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(details, textWidth), x + 6, y + 18, 0x9a9a9a);

        if (toggle != null)
        {
            toggle.draw();
        }
        else
        {
            String value = format(option, option.get());
            int center = x + RIGHT - BUTTON_SIZE - 2 - BUTTON_SIZE / 2;
            mc.fontRenderer.drawStringWithShadow(value, center - mc.fontRenderer.getStringWidth(value) / 2F, y + 11, 0xffffff);
            downButton.drawButton(mouseX, mouseY, partialTicks);
            upButton.drawButton(mouseX, mouseY, partialTicks);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
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
        return 30;
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
