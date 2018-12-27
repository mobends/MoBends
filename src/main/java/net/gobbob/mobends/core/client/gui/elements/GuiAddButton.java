package net.gobbob.mobends.core.client.gui.elements;

import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiAddButton {
	private int x, y;
	private int width, height;
	private int textureX, textureY;
	private int zLevel;
	private boolean hover, pressed;
	
	public GuiAddButton(int x, int y) {
		this.x = x;
		this.y = y;
		this.width = 16;
		this.height = 16;
		this.textureX = 112;
		this.textureY = 16;
		this.zLevel = 0;
		this.hover = false;
		this.pressed = false;
	}
	
	public GuiAddButton() {
		this(0, 0);
	}
	
	public void initGui(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void update(int mouseX, int mouseY) {
		this.hover = mouseX >= this.x && mouseX < this.x+16 &&
				     mouseY >= this.y && mouseY < this.y+16;
	}
	
	public boolean mouseClicked(int mouseX, int mouseY, int event) {
		if(this.hover) this.pressed = true;
		return this.pressed;
	}
	
	public void mouseReleased(int mouseX, int mouseY, int event) {
		this.pressed = false;
	}

	public void display() {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiBendsMenu.ICONS_TEXTURE);
		
		int tX = this.textureX;
		int tY = this.textureY + (this.hover ? 16 : 0);
		
		float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + height), (double)this.zLevel).tex((double)((float)(tX + 0) * 0.00390625F), (double)((float)(tY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), (double)this.zLevel).tex((double)((float)(tX + width) * 0.00390625F), (double)((float)(tY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), (double)this.zLevel).tex((double)((float)(tX + width) * 0.00390625F), (double)((float)(tY + 0) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(tX + 0) * 0.00390625F), (double)((float)(tY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
	}

	public void setPosition(int i, int j) {
		this.x = i;
		this.y = j;
	}
}
