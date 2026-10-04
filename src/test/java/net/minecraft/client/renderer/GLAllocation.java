package net.minecraft.client.renderer;

import goblinbob.mobends.test.geometry.CaptureAccess;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** TEST STAND-IN: display lists are numbers the capture keys vertices by. */
public class GLAllocation
{
    public static synchronized int generateDisplayLists(int range) { return CaptureAccess.newLists(range); }
    public static synchronized void deleteDisplayLists(int list, int range) { }
    public static synchronized void deleteDisplayLists(int list) { }
    public static synchronized ByteBuffer createDirectByteBuffer(int capacity) { return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()); }
    public static IntBuffer createDirectIntBuffer(int capacity) { return createDirectByteBuffer(capacity << 2).asIntBuffer(); }
    public static FloatBuffer createDirectFloatBuffer(int capacity) { return createDirectByteBuffer(capacity << 2).asFloatBuffer(); }
}
