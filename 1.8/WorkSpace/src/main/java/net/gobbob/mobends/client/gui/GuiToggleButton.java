package net.gobbob.mobends.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;

public class GuiToggleButton extends GuiButton{
	
	public boolean toggleState;
	public String title;
	public int titleWidth;
	
	public GuiToggleButton(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, boolean state) {
		super(p_i1020_1_, p_i1020_2_, p_i1020_3_, "");
		this.toggleState = state;
		
		this.width = 40;
		this.height = 20;
		
		this.displayString = state ? "ON" : "OFF";
	}
	
	public GuiToggleButton setTitle(String argTitle,int argWidth){
		this.title = argTitle;
		this.titleWidth = argWidth;
		this.xPosition+=argWidth;
		return this;
	}
	
	@Override
	public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = p_146112_1_.fontRendererObj;
            p_146112_1_.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
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
            
            this.mouseDragged(p_146112_1_, p_146112_2_, p_146112_3_);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
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
}
