package net.minecraft.client.renderer;

import goblinbob.mobends.test.geometry.CaptureAccess;
import net.minecraft.client.renderer.vertex.VertexFormat;

/** TEST STAND-IN: collects positions and texture coordinates for the capture. */
public class WorldRenderer
{
    public WorldRenderer(int size) { }
    public void begin(int mode, VertexFormat format) { }
    public WorldRenderer pos(double x, double y, double z) { CaptureAccess.pos(x, y, z); return this; }
    public WorldRenderer tex(double u, double v) { CaptureAccess.tex(u, v); return this; }
    public WorldRenderer normal(float x, float y, float z) { return this; }
    public WorldRenderer color(float r, float g, float b, float a) { return this; }
    public WorldRenderer color(int r, int g, int b, int a) { return this; }
    public WorldRenderer lightmap(int a, int b) { return this; }
    public void endVertex() { CaptureAccess.endVertex(); }
    public void finishDrawing() { }
}
