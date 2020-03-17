package goblinbob.mobends.core.client.gui.packswindow;

import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiPackTab
{

    public static final int WIDTH = 18;
    public static final int HEIGHT = 15;

    public final String titleKey;
    private final int textureIndex;
    private int x;
    private int y;
    private boolean hovered;
    private boolean selected;
    private float selectedTransitionTween;

    public GuiPackTab(String titleKey, int textureIndex)
    {
        this.titleKey = titleKey;
        this.textureIndex = textureIndex;
        this.hovered = false;
        this.selected = false;
        this.selectedTransitionTween = 0;
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

        if (selected)
        {
            this.selectedTransitionTween = Math.min(this.selectedTransitionTween + .2F, 1F);
        }
        else
        {
            this.selectedTransitionTween = Math.max(this.selectedTransitionTween - .2F, 0F);
        }
    }

    public void draw(int mouseX, int mouseY)
    {
        update(mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiPacksWindow.BACKGROUND_TEXTURE);
        final int yOffset = y - HEIGHT + (selected ? -1 : 0);
        final int SELECTED_TEXTURE_Y = 147;
        final int HOVERED_TEXTURE_Y = 132;
        final int NEUTRAL_TEXTURE_Y = 117;

        final int textureY = hovered ? HOVERED_TEXTURE_Y : NEUTRAL_TEXTURE_Y;
        Draw.texturedModalRect(x, yOffset, textureIndex * WIDTH, textureY, WIDTH, HEIGHT);

        if (this.selectedTransitionTween > 0)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            GlStateManager.color(1F, 1F, 1F, this.selectedTransitionTween);
            Draw.texturedModalRect(x, yOffset, textureIndex * WIDTH, SELECTED_TEXTURE_Y, WIDTH, HEIGHT);
            GlStateManager.color(1F, 1F, 1F, 1F);

            GlStateManager.disableBlend();
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        update(mouseX, mouseY);

        return hovered;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

}
