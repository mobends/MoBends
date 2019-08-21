package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;

public class GuiPackTab
{

    public static final int WIDTH = 18;
    public static final int HEIGHT = 16;
    public static final int OFFSET = 15;

    public final String titleKey;
    private final int textureIndex;
    private int x;
    private int y;
    public boolean hovered;

    public GuiPackTab(String titleKey, int textureIndex)
    {
        this.titleKey = titleKey;
        this.textureIndex = textureIndex;
        this.hovered = false;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + WIDTH &&
                       mouseY >= y - HEIGHT && mouseY <= y;
    }

    public void draw(int mouseX, int mouseY, boolean selected)
    {
        update(mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        int height = selected ? HEIGHT : HEIGHT - 1;
        int textureY = selected ? 149 : hovered ? 133 : 117;
        Draw.texturedModalRect(x, y - height, textureIndex * WIDTH, textureY, WIDTH, height);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        update(mouseX, mouseY);

        return hovered;
    }

}
