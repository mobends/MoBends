package net.gobbob.mobends.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class CustomFontRenderer
{
	protected CustomFont font;
	protected int characterSpacing = 1;
	
	public void setFont(CustomFont font)
	{
		this.font = font;
	}

	protected void drawSymbol(CustomFont.Symbol symbol, BufferBuilder vertexBuffer, int x, int y)
	{
		x += symbol.offsetX;
		y += symbol.offsetY;
		int width = symbol.width;
		int height = symbol.height;
		float textureX = (float)symbol.u / this.font.atlasWidth;
		float textureY = (float)symbol.v / this.font.atlasHeight;
		float textureWidth = (float)width / this.font.atlasWidth;
		float textureHeight = (float)height / this.font.atlasHeight;
		
		vertexBuffer.pos((double) x, (double) (y), 0)
				.tex(textureX, textureY + textureHeight).endVertex();
		vertexBuffer.pos((double) (x + width), (double) (y), 0)
				.tex(textureX + textureWidth, textureY + textureHeight).endVertex();
		vertexBuffer.pos((double) (x + width), (double) (y - height), 0)
				.tex(textureX + textureWidth, textureY).endVertex();
		vertexBuffer.pos((double) x, (double) (y - height), 0)
				.tex(textureX, textureY).endVertex();
	}

	public int getTextWidth(String textToDraw)
	{
		int width = 0;
		for (int i = 0; i < textToDraw.length(); ++i)
		{
			CustomFont.Symbol symbol = this.font.getSymbol(textToDraw.charAt(i));
			width += symbol.width;
			if (i != textToDraw.length() - 1)
				width += characterSpacing;
		}
		return width;
	}
	
	public void drawText(String textToDraw, int x, int y)
	{
		if (this.font == null)
			return;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.font.resourceLocation);
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        int nextCharX = x;
		for (int i = 0; i < textToDraw.length(); ++i)
		{
			CustomFont.Symbol symbol = this.font.getSymbol(textToDraw.charAt(i));
			this.drawSymbol(symbol, vertexbuffer, nextCharX, y);
			nextCharX += symbol.width + characterSpacing;
		}
		tessellator.draw();
	}
	
	public void drawCenteredText(String textToDraw, int x, int y)
	{
		int width = this.getTextWidth(textToDraw);
		this.drawText(textToDraw, x - width / 2, y);
	}
}
