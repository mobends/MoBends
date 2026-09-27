package net.minecraft.client.renderer;

import java.nio.FloatBuffer;

/** All GL calls are no-ops in the lab. */
public class GlStateManager
{
    public static void translate(float x, float y, float z) {}
    public static void translate(double x, double y, double z) {}
    public static void scale(float x, float y, float z) {}
    public static void scale(double x, double y, double z) {}
    public static void multMatrix(FloatBuffer matrix) {}
    public static void glVertex3f(float x, float y, float z) {}
    public static void pushMatrix() {}
    public static void popMatrix() {}
}
