package net.gobbob.mobends.core.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class Draw
{

    public static void rectangle(int left, int top, int width, int height, int color)
    {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, alpha);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double) left, (double) top + height, 0.0D).endVertex();
        vertexbuffer.pos((double) left + width, (double) top + height, 0.0D).endVertex();
        vertexbuffer.pos((double) left + width, (double) top, 0.0D).endVertex();
        vertexbuffer.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void rectangle(float left, float top, float width, float height)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double) left, (double) top + height, 0.0D).endVertex();
        vertexbuffer.pos((double) left + width, (double) top + height, 0.0D).endVertex();
        vertexbuffer.pos((double) left + width, (double) top, 0.0D).endVertex();
        vertexbuffer.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void rectangle(float x, float y, float w, float h, int color)
    {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
        rectangle(x, y, w, h);
    }

    public static void rectangle_xgradient(float x, float y, float w, float h, IColorRead color0, IColorRead color1)
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        GlStateManager.glBegin(GL11.GL_QUADS);
        GlStateManager.color(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        GlStateManager.glTexCoord2f(0, 0); GlStateManager.glVertex3f((float) (x + 0), (float) (y + 0), (float) 0);
        GlStateManager.glTexCoord2f(0, 1); GlStateManager.glVertex3f((float) (x + 0), (float) (y + h), (float) 0);
        GlStateManager.color(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        GlStateManager.glTexCoord2f(1, 1); GlStateManager.glVertex3f((float) (x + w), (float) (y + h), (float) 0);
        GlStateManager.glTexCoord2f(1, 0); GlStateManager.glVertex3f((float) (x + w), (float) (y + 0), (float) 0);
        GlStateManager.glEnd();

        GlStateManager.enableTexture2D();
    }

    public static void rectangle_ygradient(float x, float y, float w, float h, IColorRead color0, IColorRead color1)
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();

        GlStateManager.glBegin(GL11.GL_QUADS);
        GlStateManager.color(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        GlStateManager.glTexCoord2f(1, 0); GlStateManager.glVertex3f((float) (x + w), (float) (y + 0), (float) 0);
        GlStateManager.glTexCoord2f(0, 0); GlStateManager.glVertex3f((float) (x + 0), (float) (y + 0), (float) 0);
        GlStateManager.color(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        GlStateManager.glTexCoord2f(0, 1); GlStateManager.glVertex3f((float) (x + 0), (float) (y + h), (float) 0);
        GlStateManager.glTexCoord2f(1, 1); GlStateManager.glVertex3f((float) (x + w), (float) (y + h), (float) 0);
        GlStateManager.glEnd();

        GlStateManager.enableTexture2D();
    }

    public static void rectangle_xgradient(float x, float y, float width, float height, int color0, int color1)
    {
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double) x, (double) y + height, 0.0D).color(r0, g0, b0, a0).endVertex();
        vertexbuffer.pos((double) x + width, (double) y + height, 0.0D).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos((double) x + width, (double) y, 0.0D).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos((double) x, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void rectangle_ygradient(float x, float y, float w, float h, int color0, int color1)
    {
        GlStateManager.enableBlend();
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double) x, (double) y + h, 0.0D).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos((double) x + w, (double) y + h, 0.0D).color(r1, g1, b1, a1).endVertex();
        vertexbuffer.pos((double) x + w, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        vertexbuffer.pos((double) x, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void circle(double x, double y, double radius, int vertices)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        vertexbuffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        for (int i = 0; i < vertices; i++)
        {
            double angle = ((double) i / vertices) * Math.PI * 2;
            vertexbuffer.pos((double) (x + Math.cos(angle) * radius), (double) (y + Math.sin(angle) * radius), 0.0D).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void circle(double x, double y, double radius)
    {
        circle(x, y, radius, 100);
    }

	public static void cube(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, IColorRead color)
	{
		final float r = color.getR();
		final float g = color.getG();
		final float b = color.getB();
		final float a = color.getA();

		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        // NEG_X
        vertexbuffer.pos(minX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        vertexbuffer.pos(minX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        vertexbuffer.pos(minX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        vertexbuffer.pos(minX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        // POS_X
        vertexbuffer.pos(maxX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        vertexbuffer.pos(maxX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        // NEG_Z
        vertexbuffer.pos(maxX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        vertexbuffer.pos(minX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        vertexbuffer.pos(minX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        // POS_Z
        vertexbuffer.pos(minX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        vertexbuffer.pos(minX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        // NEG_Y
        vertexbuffer.pos(minX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        vertexbuffer.pos(minX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).tex(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        // POS_Y
        vertexbuffer.pos(maxX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        vertexbuffer.pos(maxX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        vertexbuffer.pos(minX, maxY, minZ).tex(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        vertexbuffer.pos(minX, maxY, maxZ).tex(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        tessellator.draw();
	}

	public static void texturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + height), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), 0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), 0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

	public static void texturedModalRect(int x, int y, int width, int height, int textureX, int textureY, int textureWidth, int textureHeight)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + height), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + textureHeight) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), 0).tex((double)((float)(textureX + textureWidth) * 0.00390625F), (double)((float)(textureY + textureHeight) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), 0).tex((double)((float)(textureX + textureWidth) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public static void texturedRectangle(int x, int y, int width, int height, float textureX, float textureY, float textureWidth, float textureHeight)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double) (x + 0), (double) (y + height), 0).tex(textureX, textureY + textureHeight).endVertex();
        vertexbuffer.pos((double) (x + width), (double) (y + height), 0).tex(textureX + textureWidth, textureY + textureHeight).endVertex();
        vertexbuffer.pos((double) (x + width), (double) (y + 0), 0).tex(textureX + textureWidth, textureY).endVertex();
        vertexbuffer.pos((double) (x + 0), (double) (y + 0), 0).tex(textureX, textureY).endVertex();
        tessellator.draw();
    }
    //Trimmed - HalfStretched

    public static void thsPuzzle(int x, int y, int left, int middle, int right, int height, int textureX, int textureY)
    {
        Draw.texturedModalRect(x, y, textureX, textureY, left, height);
        Draw.texturedModalRect(x + left, y, middle, height, textureX + left, textureY, 1, height);
        Draw.texturedModalRect(x + left + middle, y, textureX + left + 1, textureY, right, height);
    }

    public static void borderBox(int x, int y, int width, int height, int border, int textureX, int textureY)
    {
		/* Top-Left		*/ Draw.texturedModalRect(x-border, y-border, textureX, textureY, border, border);
		/* Top 			*/ Draw.texturedModalRect(x, y-border, width, border, textureX+border, textureY, 1, border);
		/* Top-Right 	*/ Draw.texturedModalRect(x+width, y-border, textureX+border+1, textureY, border, border);
		/* Right 		*/ Draw.texturedModalRect(x+width, y, border, height, textureX+border+1, textureY+border, border, 1);
		/* Bottom-Right */ Draw.texturedModalRect(x+width, y+height, textureX+border+1, textureY+border+1, border, border);
		/* Bottom		*/ Draw.texturedModalRect(x, y+height, width, border, textureX+border, textureY+border+1, 1, border);
		/* Bottom-Left	*/ Draw.texturedModalRect(x-border, y+height, textureX, textureY+border+1, border, border);
		/* Left 		*/ Draw.texturedModalRect(x-border, y, border, height, textureX, textureY+border, border, 1);
		/* Inside		*/ Draw.texturedModalRect(x, y, width, height, textureX+border, textureY+border, 1, 1);
	}

    public static void line(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, IColorRead color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        vertexbuffer.pos(minX, minY, minZ).color(color.getR(), color.getG(), color.getB(), color.getA()).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).color(color.getR(), color.getG(), color.getB(), color.getA()).endVertex();

        tessellator.draw();
    }

}
