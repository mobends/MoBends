package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiSmallToggleButton
{

	protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;

    protected int x;
    protected int y;
    protected boolean hovered;
    protected boolean enabled;
    protected boolean toggleState;

    public GuiSmallToggleButton()
    {
        this.x = 0;
        this.y = 0;
        this.enabled = true;
        this.hovered = false;
        this.toggleState = false;
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
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        int k = this.hovered ? 1 : 0;
        
        GlStateManager.pushMatrix();
        	if(this.toggleState)
                GlStateManager.color(0.3F, 1.0F, 0.7F);
        	else
                GlStateManager.color(1.0F, 0.3F, 0.4F);

        	Draw.texturedModalRect(this.x, this.y, 0, 66 + k * 20, WIDTH / 2, HEIGHT);
        	Draw.texturedModalRect(this.x + WIDTH / 2, this.y, 200 - WIDTH / 2, 66 + k * 20, WIDTH / 2, HEIGHT);
        GlStateManager.popMatrix();
        
        int l = 14737632;

        if (!this.enabled)
        {
            l = 10526880;
        }
        else if (this.hovered)
        {
            l = 16777120;
        }
        
        String stateText = this.toggleState ? "ON" : "OFF";
        int textWidth = fontRenderer.getStringWidth(stateText);
        fontRenderer.drawString(stateText, this.x - textWidth/2 + WIDTH /2, this.y + (HEIGHT - 8) / 2, l);

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void setToggleState(boolean state)
    {
    	this.toggleState = state;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
    	if (hovered && button == 0)
    	{
	    	this.toggleState = !this.toggleState;
	        return true;
    	}
    	
		return false;
    }
    
    public boolean getToggleState()
    {
    	return this.toggleState;
    }

}
