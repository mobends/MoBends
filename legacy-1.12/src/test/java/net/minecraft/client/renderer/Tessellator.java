package net.minecraft.client.renderer;

import goblinbob.mobends.test.geometry.CaptureAccess;

/** TEST STAND-IN: drawing hands the buffer's vertices to the capture. */
public class Tessellator
{
    private static final Tessellator INSTANCE = new Tessellator(0);
    private final BufferBuilder buffer = new BufferBuilder(0);

    public Tessellator(int size) { }
    public static Tessellator getInstance() { return INSTANCE; }
    public BufferBuilder getBuffer() { return buffer; }
    public void draw() { CaptureAccess.draw(); }
}
