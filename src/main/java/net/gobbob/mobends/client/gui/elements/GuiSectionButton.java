package net.gobbob.mobends.client.gui.elements;

import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.gui.CustomFont;
import net.gobbob.mobends.client.gui.CustomFontRenderer;
import net.gobbob.mobends.client.gui.GuiBendsMenu;
import net.gobbob.mobends.main.ModStatics;
import net.gobbob.mobends.util.Color;
import net.gobbob.mobends.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GuiSectionButton
{
	public static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
			"textures/gui/buttons.png");

	// Expressed in ticks
	public static final float hoverAnimationDuration = 2.0F;
	
	private String label;
	private int x, y;
	private int width, height;
	private int bgTextureU, bgTextureV;
	private Color neutralColor;
	private Color bgColor;
	private boolean hasLeftIcon, hasRightIcon;
	private int leftIconU, leftIconV, leftIconWidth, leftIconHeight;
	private int rightIconU, rightIconV, rightIconWidth, rightIconHeight;
	private int zLevel;
	private CustomFontRenderer fontRenderer;
	
	private boolean hover, pressed;
	private float ticksAfterHovered = 0.0F;

	public GuiSectionButton(int x, int y, String label, Color bgColor)
	{
		this.label = label;
		this.x = x;
		this.y = y;
		this.width = 318;
		this.height = 43;
		this.bgTextureU = 0;
		this.bgTextureV = 0;
		this.neutralColor = Color.fromHex(0xFF777777);
		this.bgColor = bgColor;
		this.hasLeftIcon = this.hasRightIcon = false;
		this.zLevel = 0;
		this.hover = false;
		this.pressed = false;
		
		this.fontRenderer = new CustomFontRenderer();
		this.fontRenderer.setFont(CustomFont.BOLD);
	}

	public GuiSectionButton(String label, Color bgColor)
	{
		this(0, 0, label, bgColor);
	}
	
	public GuiSectionButton(String label, int bgColor)
	{
		this(0, 0, label, Color.fromHex(bgColor));
	}

	public GuiSectionButton setLeftIcon(int u, int v, int width, int height)
	{
		this.hasLeftIcon = true;
		this.leftIconU = u;
		this.leftIconV = v;
		this.leftIconWidth = width;
		this.leftIconHeight = height;
		return this;
	}
	
	public GuiSectionButton setRightIcon(int u, int v, int width, int height)
	{
		this.hasRightIcon = true;
		this.rightIconU = u;
		this.rightIconV = v;
		this.rightIconWidth = width;
		this.rightIconHeight = height;
		return this;
	}
	
	public void initGui(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public void update(int mouseX, int mouseY)
	{
		boolean nowHover = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y
				&& mouseY < this.y + this.height;
		
		if (nowHover && !this.hover)
		{
			this.onHover();
		}
		
		this.hover = nowHover;
	}

	public boolean mouseClicked(int mouseX, int mouseY, int event)
	{
		if (this.hover)
			this.pressed = true;
		return this.pressed;
	}

	public void mouseReleased(int mouseX, int mouseY, int event)
	{
		this.pressed = false;
	}
	
	public void onHover()
	{
		this.ticksAfterHovered = 0F;
	}
	
	public void display()
	{
		this.ticksAfterHovered += DataUpdateHandler.ticksPerFrame;
		
		if (this.hover)
			GlStateManager.color(this.bgColor.r, this.bgColor.g, this.bgColor.b, this.bgColor.a);
		else
			GlStateManager.color(this.neutralColor.r, this.neutralColor.g, this.neutralColor.b, this.neutralColor.a);
		Minecraft.getMinecraft().getTextureManager().bindTexture(BUTTONS_TEXTURE);
		
		int tX = this.bgTextureU;
		int tY = this.bgTextureV;

		float uScale = 0.001953125F;
		float vScale = 0.0078125F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		
		vertexbuffer.pos((double) (x + 0), (double) (y + height), (double) this.zLevel)
				.tex((double) ((float) (tX + 0) * uScale), (double) ((float) (tY + height) * vScale))
				.endVertex();
		vertexbuffer.pos((double) (x + width), (double) (y + height), (double) this.zLevel)
				.tex((double) ((float) (tX + width) * uScale), (double) ((float) (tY + height) * vScale))
				.endVertex();
		vertexbuffer.pos((double) (x + width), (double) (y + 0), (double) this.zLevel)
				.tex((double) ((float) (tX + width) * uScale), (double) ((float) (tY + 0) * vScale))
				.endVertex();
		vertexbuffer.pos((double) (x + 0), (double) (y + 0), (double) this.zLevel)
				.tex((double) ((float) (tX + 0) * uScale), (double) ((float) (tY + 0) * vScale)).endVertex();
		tessellator.draw();
		
		if (this.hover)
		{
			float scale = 1.0F;
			if (this.ticksAfterHovered < hoverAnimationDuration)
			{
				float PI = (float) Math.PI;
				float t = this.ticksAfterHovered / hoverAnimationDuration;
				scale = (1F - MathHelper.cos(t * PI * 1.5F));
				scale = MathHelper.sqrt(scale);
			}
			
			int iconSpacing = 30;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + iconSpacing, y + height/2, 0);
			GlStateManager.scale(scale, scale, 1);
			Draw.texturedRectangle(-leftIconWidth/2, -leftIconHeight/2,
					leftIconWidth, leftIconHeight,
					leftIconU * uScale, leftIconV * vScale,
					leftIconWidth * uScale, leftIconHeight * vScale);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + width - iconSpacing, y + height/2, 0);
			GlStateManager.scale(scale, scale, 1);
			Draw.texturedRectangle(-rightIconWidth/2, -rightIconHeight/2,
					rightIconWidth, rightIconHeight,
					rightIconU * uScale, rightIconV * vScale,
					rightIconWidth * uScale, rightIconHeight * vScale);
			GlStateManager.popMatrix();
			
		}
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.fontRenderer.drawCenteredText(this.label, x + width / 2, y + height / 2 + 6);
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void setPosition(int i, int j)
	{
		this.x = i;
		this.y = j;
	}
}
