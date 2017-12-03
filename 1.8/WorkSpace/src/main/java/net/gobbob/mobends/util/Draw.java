package net.gobbob.mobends.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class Draw {
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
    	
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(color0.r,color0.g,color0.b,color0.a);
	        GL11.glTexCoord2f(0,0); GL11.glVertex3f((float)(x + 0), (float)(y + 0), (float)0);
	        GL11.glTexCoord2f(0,1); GL11.glVertex3f((float)(x + 0), (float)(y + h), (float)0);
	        GL11.glColor4f(color1.r,color1.g,color1.b,color1.a);
	        GL11.glTexCoord2f(1,1); GL11.glVertex3f((float)(x + w), (float)(y + h), (float)0);
	        GL11.glTexCoord2f(1,0); GL11.glVertex3f((float)(x + w), (float)(y + 0), (float)0);
	    GL11.glEnd();
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
}
