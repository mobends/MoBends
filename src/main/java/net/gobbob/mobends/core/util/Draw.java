package net.gobbob.mobends.core.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Draw
{
	
	public static void rectangle(float x, float y, float w, float h){
		GL11.glBegin(GL11.GL_QUADS);
	        GL11.glTexCoord2f(0,0); GL11.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
	        GL11.glTexCoord2f(0,1); GL11.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,1); GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,0); GL11.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	    GL11.glEnd();
	}
	
	public static void rectangle_xgradient(float x, float y, float w, float h,Color color0,Color color1){
		GL11.glEnable(GL11.GL_BLEND);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
    	GlStateManager.disableTexture2D();
    	
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(color0.r,color0.g,color0.b,color0.a);
	        GL11.glTexCoord2f(0,0); GL11.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
	        GL11.glTexCoord2f(0,1); GL11.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GL11.glColor4f(color1.r,color1.g,color1.b,color1.a);
	        GL11.glTexCoord2f(1,1); GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,0); GL11.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	    GL11.glEnd();
	    
	    GlStateManager.enableTexture2D();
	}
	
	public static void rectangle_ygradient(float x, float y, float w, float h, Color color0, Color color1){
		GL11.glEnable(GL11.GL_BLEND);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
    	GlStateManager.disableTexture2D();
    	
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(color0.r,color0.g,color0.b,color0.a);
			GL11.glTexCoord2f(1,0); GL11.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	        GL11.glTexCoord2f(0,0); GL11.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
	        GL11.glColor4f(color1.r,color1.g,color1.b,color1.a);
	        GL11.glTexCoord2f(0,1); GL11.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,1); GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	    GL11.glEnd();
	    
	    GlStateManager.enableTexture2D();
	}
	
	public static void rectangle_xgradient(float x, float y, float w, float h,int color0,int color1){
		GL11.glEnable(GL11.GL_BLEND);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
    	GlStateManager.disableTexture2D();
    	
    	float a0 = (float)(color0 >> 24 & 255) / 255.0F;
    	float r0 = (float)(color0 >> 16 & 255) / 255.0F;
    	float g0 = (float)(color0 >> 8 & 255) / 255.0F;
    	float b0 = (float)(color0 & 255) / 255.0F;
    	float a1 = (float)(color1 >> 24 & 255) / 255.0F;
    	float r1 = (float)(color1 >> 16 & 255) / 255.0F;
    	float g1 = (float)(color1 >> 8 & 255) / 255.0F;
    	float b1 = (float)(color1 & 255) / 255.0F;
    	
    	GlStateManager.glBegin(GL11.GL_QUADS);
    		GlStateManager.color(r0, g0, b0, a0);
    		GlStateManager.glTexCoord2f(0,0); GlStateManager.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
    		GlStateManager.glTexCoord2f(0,1); GlStateManager.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GlStateManager.color(r1, g1, b1, a1);
	        GlStateManager.glTexCoord2f(1,1); GlStateManager.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	        GlStateManager.glTexCoord2f(1,0); GlStateManager.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	    GlStateManager.glEnd();
	    
	    GlStateManager.enableTexture2D();
	}
	
	public static void rectangle_ygradient(float x, float y, float w, float h,int color0,int color1){
		GL11.glEnable(GL11.GL_BLEND);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
    	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
    	GL11.glShadeModel(GL11.GL_SMOOTH);
    	GlStateManager.disableTexture2D();
    	
    	float a0 = (float)(color0 >> 24 & 255) / 255.0F;
    	float r0 = (float)(color0 >> 16 & 255) / 255.0F;
    	float g0 = (float)(color0 >> 8 & 255) / 255.0F;
    	float b0 = (float)(color0 & 255) / 255.0F;
    	float a1 = (float)(color1 >> 24 & 255) / 255.0F;
    	float r1 = (float)(color1 >> 16 & 255) / 255.0F;
    	float g1 = (float)(color1 >> 8 & 255) / 255.0F;
    	float b1 = (float)(color1 & 255) / 255.0F;
    	
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(r0, g0, b0, a0);
			GL11.glTexCoord2f(1,0); GL11.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	        GL11.glTexCoord2f(0,0); GL11.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
	        GL11.glColor4f(r1, g1, b1, a1);
	        GL11.glTexCoord2f(0,1); GL11.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,1); GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	    GL11.glEnd();
	    
	    GlStateManager.enableTexture2D();
	}
	
	private static void setVertexForCircle(float angle, float radius){
		Vector3f vec = new Vector3f((float)Math.cos(angle/180.0f*Math.PI),0,(float)Math.sin(angle/180.0f*Math.PI));
		vec = (Vector3f) vec.normalise();
		vec.scale(radius);
		GL11.glVertex3f(vec.x,vec.y,vec.z);
	}
	
	public static void circle(float radius){
		GL11.glBegin(GL11.GL_LINE_LOOP);
			float vertices = 360;
			for(int i = 0; i < vertices; i++){
				setVertexForCircle(((float)i)/vertices*360.0f, radius);
			}
		GL11.glEnd();
	}
	
	public static void cube(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color)
	{
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        // NEG_X
        vertexbuffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        // POS_X
        vertexbuffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        // NEG_Z
        vertexbuffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        // POS_Z
        vertexbuffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        // NEG_Y
        vertexbuffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        // POS_Y
        vertexbuffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
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
        vertexbuffer.pos((double)(x + 0), (double)(y + height), 0).tex(textureX, textureY + textureHeight).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + height), 0).tex(textureX + textureWidth, textureY + textureHeight).endVertex();
        vertexbuffer.pos((double)(x + width), (double)(y + 0), 0).tex(textureX + textureWidth, textureY).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), 0).tex(textureX, textureY).endVertex();
        tessellator.draw();
    }
	
	//Trimmed - HalfStretched
	public static void thsPuzzle(int x, int y, int left, int middle, int right, int height, int textureX, int textureY) {
		Draw.texturedModalRect(x, y, textureX, textureY, left, height);
        Draw.texturedModalRect(x+left, y, middle, height, textureX+left, textureY, 1, height);
        Draw.texturedModalRect(x+left+middle, y, textureX+left+1, textureY, right, height);
	}
	
	public static void rectangle(int left, int top, int width, int height, int color)
    {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double)left, (double)top+height, 0.0D).endVertex();
        vertexbuffer.pos((double)left+width, (double)top+height, 0.0D).endVertex();
        vertexbuffer.pos((double)left+width, (double)top, 0.0D).endVertex();
        vertexbuffer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
	
	public static void borderBox(int x, int y, int width, int height, int border, int textureX, int textureY) {
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
	
	public static void line(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color)
	{
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        vertexbuffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        vertexbuffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        
        tessellator.draw();
	}
}
