package goblinbob.mobends.core.client.gui.popup;

import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.client.gui.elements.GuiCustomButton;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class GuiPopUp
{

    protected int x, y;
    protected int width;
    protected int height;
    private String[] title;
    protected FontRenderer fontRenderer;
    private List<Button> buttons;

    public GuiPopUp(String title, ButtonProps[] buttonProps)
    {
        this(title, 120, 60, buttonProps);
    }

    public GuiPopUp(String title, int width, int height, ButtonProps[] buttonProps)
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.width = width;
        this.height = height;
        this.title = GUtil.wrapText(fontRenderer, title, width - 20);
        this.buttons = new ArrayList<>();

        for (ButtonProps buttonProp : buttonProps)
        {
            this.buttons.add(new Button(fontRenderer, buttonProp));
        }
    }

    public void initGui(int x, int y)
    {
        this.x = x - width / 2;
        this.y = y - height / 2;

        int offset = this.x + width - 5;
        for (Button button : buttons)
        {
            button.buttonUI.setPosition(offset - button.buttonUI.width, this.y + height - 25);
            offset -= button.buttonUI.width + 5;
        }
    }

    public void update(int mouseX, int mouseY)
    {
        // No default functionality.
    }

    public void display(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        /* Top-Left		*/
        Draw.texturedModalRect(x - 4, y - 4, 60, 64, 4, 4);
        /* Top 			*/
        Draw.texturedModalRect(x, y - 4, width, 4, 64, 64, 1, 4);
        /* Top-Right 	*/
        Draw.texturedModalRect(x + width, y - 4, 65, 64, 4, 4);
        /* Right 		*/
        Draw.texturedModalRect(x + width, y, 4, height, 65, 68, 4, 1);
        /* Bottom-Right */
        Draw.texturedModalRect(x + width, y + height, 65, 69, 4, 4);
        /* Bottom		*/
        Draw.texturedModalRect(x, y + height, width, 4, 64, 69, 1, 4);
        /* Bottom-Left	*/
        Draw.texturedModalRect(x - 4, y + height, 60, 69, 4, 4);
        /* Left 		*/
        Draw.texturedModalRect(x - 4, y, 4, height, 60, 68, 4, 1);
        /* Inside		*/
        Draw.texturedModalRect(x, y, width, height, 64, 68, 1, 1);

        int yOffset = 6;
        for (String line : title)
        {
            fontRenderer.drawStringWithShadow(line, x + (width - fontRenderer.getStringWidth(line)) / 2, y + yOffset, 0xffffff);
            yOffset += 9;
        }

        for (Button button : buttons)
        {
            button.buttonUI.drawButton(mouseX, mouseY, partialTicks);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int state)
    {
        for (Button button : buttons)
        {
            if (button.buttonUI.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
            {
                // Performing the button action.
                button.props.action.performAction();
                return;
            }
        }
    }

    @FunctionalInterface
    public interface ButtonAction
    {
        void performAction();
    }

    public static class ButtonProps
    {
        private String label;
        private ButtonAction action;

        public ButtonProps(String label, ButtonAction action)
        {
            this.label = label;
            this.action = action;
        }
    }

    public static class Button
    {
        public ButtonProps props;
        public GuiCustomButton buttonUI;

        public Button(FontRenderer fontRenderer, ButtonProps props)
        {
            this.props = props;
            int labelWidth = fontRenderer.getStringWidth(props.label);
            this.buttonUI = new GuiCustomButton(Math.max(labelWidth + 10, 50), 20, props.label);
        }
    }

}
