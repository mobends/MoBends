package org.lwjgl.opengl;

import goblinbob.mobends.test.geometry.CaptureAccess;

/**
 * TEST STAND-IN: 1.8.9's ModelRenderer (and the mod's ModelPart, as it) compiles its display lists with GL11 itself,
 * not through GlStateManager; recorded as the GlStateManager stand-in records them (see {@code Capture}); no GL.
 */
public class GL11
{
    public static final int GL_COMPILE = 4864;

    public static void glNewList(int list, int mode) { CaptureAccess.beginList(list); }
    public static void glEndList() { CaptureAccess.endList(); }
    public static void glCallList(int list) { CaptureAccess.callList(list); }
}
