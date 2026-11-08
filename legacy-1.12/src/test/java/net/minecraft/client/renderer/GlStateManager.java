package net.minecraft.client.renderer;

import goblinbob.mobends.test.geometry.CaptureAccess;

import java.nio.FloatBuffer;

/** TEST STAND-IN: records the matrix stack and display lists (see {@code Capture}); no GL. */
public class GlStateManager
{
    public static void pushMatrix() { CaptureAccess.push(); }
    public static void popMatrix() { CaptureAccess.pop(); }
    public static void translate(float x, float y, float z) { CaptureAccess.translate(x, y, z); }
    public static void translate(double x, double y, double z) { CaptureAccess.translate(x, y, z); }
    public static void scale(float x, float y, float z) { CaptureAccess.scale(x, y, z); }
    public static void scale(double x, double y, double z) { CaptureAccess.scale(x, y, z); }
    public static void rotate(float angle, float x, float y, float z) { CaptureAccess.rotate(angle, x, y, z); }
    public static void multMatrix(FloatBuffer matrix)
    {
        double[] m = new double[16];
        for (int i = 0; i < 16; i++) m[i] = matrix.get(matrix.position() + i);
        CaptureAccess.multiply(m);
    }
    public static void callList(int list) { CaptureAccess.callList(list); }
    public static void glNewList(int list, int mode) { CaptureAccess.beginList(list); }
    public static void glEndList() { CaptureAccess.endList(); }
    public static void enableRescaleNormal() { }
    public static void disableRescaleNormal() { }
    public static void enableBlend() { }
    public static void disableBlend() { }
    public static void enableCull() { }
    public static void disableCull() { }
    public static void color(float r, float g, float b, float a) { }
    public static void color(float r, float g, float b) { }
}
