package net.gobbob.mobends.core.client.gui.elements;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class GuiToggleButton
{

	protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
	
    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    protected int x, y;
    protected boolean hovered;
    protected boolean enabled;
    protected boolean toggleState;
    protected final String title;
    protected final int titleWidth;
    
    public GuiToggleButton(String title, int titleWidth)
    {
        this.x = 0;
        this.y = 0;
        this.enabled = true;
        this.hovered = false;
        this.toggleState = false;
        
        this.title = title;
        this.titleWidth = titleWidth;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    public void draw()
    {
        /*GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
        int textureY = hovered ? 64 : 44;
        Draw.texturedModalRect(x, y, 88, textureY, WIDTH, HEIGHT);*/
        
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        int k = 0;
        
        GL11.glPushMatrix();
        	GL11.glColor3f(0.5f,0.5f,0.5f);
        	Draw.texturedModalRect(this.x - this.titleWidth, this.y, 0, 46 + k * 20, (this.titleWidth + WIDTH) / 2, HEIGHT);
        	Draw.texturedModalRect(this.x + this.titleWidth / 2 - this.titleWidth, this.y, 200 - (this.titleWidth + WIDTH) / 2, 46 + k * 20, (this.titleWidth + WIDTH) / 2, HEIGHT);
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        	if(this.toggleState)
        		GL11.glColor3f(0,1,0);
        	else
        		GL11.glColor3f(1,0,0);
        	Draw.texturedModalRect(this.titleWidth + this.x - this.titleWidth, this.y, 0, 46 + k * 20, WIDTH / 2, HEIGHT);
        	Draw.texturedModalRect(this.titleWidth + this.x + WIDTH / 2 - this.titleWidth, this.y, 200 - WIDTH / 2, 46 + k * 20, WIDTH / 2, HEIGHT);
        GL11.glPopMatrix();
        
        //this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
        int l = 14737632;

        if (!this.enabled)
        {
            l = 10526880;
        }
        else if (this.hovered)
        {
            l = 16777120;
        }
        fontRenderer.drawString(this.title, this.x + 15  - this.titleWidth, this.y + (HEIGHT - 8) / 2, l);
        //fontRenderer.drawCenteredString(fontRenderer, this.title, this.x + WIDTH / 2, this.y + (HEIGHT - 8) / 2, l);
    
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int state)
    {
        return hovered;
    }

}
