package net.gobbob.mobends.client.gui;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.configuration.SettingBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class GuiSettingsBoolean extends GuiSettingsNode {
	protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    
    public String displayString;
    protected boolean hovered;
    
	public boolean toggleState;
	public String title;
	public int titleWidth;
	
	public GuiSettingsBoolean(SettingBoolean settingsNode, int xPosition, int yPosition) {
		super(settingsNode, xPosition, yPosition);
		
		this.width = 40;
		this.height = 20;
		
		this.toggleState = settingsNode.isEnabled();
		this.displayString = this.toggleState ? "ON" : "OFF";
	}
	
	public GuiSettingsBoolean setTitle(String argTitle,int argWidth){
		this.title = argTitle;
		this.titleWidth = argWidth;
		this.xPosition += argWidth;
		return this;
	}

	@Override
	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.visible)
        {
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.xPosition-this.titleWidth && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            GL11.glPushMatrix();
	        	GL11.glColor3f(0.5f,0.5f,0.5f);
	        	this.drawTexturedModalRect(this.xPosition - this.titleWidth, this.yPosition, 0, 46 + k * 20, (this.titleWidth+this.width) / 2, this.height);
	        	this.drawTexturedModalRect(this.xPosition + this.titleWidth / 2 - this.titleWidth, this.yPosition, 200 - (this.titleWidth+this.width) / 2, 46 + k * 20, (this.titleWidth+this.width) / 2, this.height);
	        GL11.glPopMatrix();
	           
            GL11.glPushMatrix();
            	if(this.toggleState)
            		GL11.glColor3f(0,1,0);
            	else
            		GL11.glColor3f(1,0,0);
            	this.drawTexturedModalRect(this.titleWidth + this.xPosition - this.titleWidth, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
            	this.drawTexturedModalRect(this.titleWidth + this.xPosition + this.width / 2 - this.titleWidth, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            GL11.glPopMatrix();
            
            this.mouseDragged(minecraft, mouseX, mouseY);
            int l = 14737632;

            if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.hovered)
            {
                l = 16777120;
            }
            this.drawString(fontrenderer, this.title, this.xPosition + 15  - this.titleWidth, this.yPosition + (this.height - 8) / 2, l);
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
        
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
	}
	
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }
	
	protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

	public void toggle() {
		toggleState = !toggleState;
		settingsNode.set(toggleState);
		this.displayString = toggleState ? "ON" : "OFF";
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if(this.enabled && this.visible && this.hovered) {
			toggle();
			return true;
		}
		return false;
	}
}
