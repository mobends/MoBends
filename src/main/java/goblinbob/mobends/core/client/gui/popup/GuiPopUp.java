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
    private List<GuiCustomButton> buttons;
    private int afterAction = 0;

    public GuiPopUp(String title, int action, String[] buttons)
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.width = 120;
        this.height = 60;
        this.title = GUtil.squashText(fontRenderer, title, width - 40);
        this.buttons = new ArrayList<>();
        for (String label : buttons)
        {
            this.buttons.add(new GuiCustomButton(50, 20, label));
        }
        this.afterAction = action;
    }

    public void initGui(int x, int y)
    {
        this.x = x - width / 2;
        this.y = y - height / 2;

        for (int index = 0; index < buttons.size(); index++)
        {
            GuiCustomButton button = buttons.get(index);
            button.setPosition(this.x + 5 + index * 60, this.y + height - 25);
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
        int yOffset = 0;
        for (String line : title)
        {
            fontRenderer.drawStringWithShadow(line, x + (width - fontRenderer.getStringWidth(line)) / 2, y + 3 + yOffset, 0xffffff);
            yOffset += 9;
        }

        for (GuiCustomButton button : buttons)
        {
            button.drawButton(mouseX, mouseY, partialTicks);
        }
    }

    public int mouseClicked(int mouseX, int mouseY, int state)
    {
        for (int index = 0; index < buttons.size(); index++)
        {
            GuiButton button = buttons.get(index);
            if (button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
            {
                return index;
            }
        }
        return -1;
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        // No default functionality.
    }

    public int getAfterAction()
    {
        return afterAction;
    }

}
