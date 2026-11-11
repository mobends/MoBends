package goblinbob.mobends.core.util;

//? if >=26.1 {
/*import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.renderer.RenderPipelines;
*///?} else {
import com.mojang.blaze3d.systems.RenderSystem;
//?}
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.12's drawing helpers the Mo' Bends menu uses, and the GUI's calls that differ between Minecraft
 * versions (26.x's GUI extracts what's drawn, and its pose is 2D). Textures are drawn blended.
 */
public final class Draw {
    private static final int TEXTURE_SIZE = 256;

    private Draw() {
    }

    /** A part of a 256×256 texture, drawn at its size. */
    public static void texturedModalRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int textureX, int textureY, int width, int height) {
        blit(graphics, texture, x, y, width, height, textureX, textureY, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    /** A part of a 256×256 texture, stretched to {@code width} × {@code height}. */
    public static void texturedModalRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                         int textureX, int textureY, int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }
        blit(graphics, texture, x, y, width, height, textureX, textureY, textureWidth, textureHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    /**
     * The part of a {@code textureWidth} × {@code textureHeight} texture at ({@code u}, {@code v}),
     * {@code uWidth} × {@code vHeight} big, stretched to {@code width} × {@code height}.
     */
    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                            int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight);
        *///?} else {
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        //?}
    }

    /**
     * A box with a border {@code border} wide around a {@code width} × {@code height} inside, from a
     * part of the texture with its corners {@code border} wide, one pixel of edge between them and
     * one of inside: the edges and the inside are stretched.
     */
    public static void borderBox(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int border, int textureX, int textureY) {
        /* Top-Left     */ texturedModalRect(graphics, texture, x - border, y - border, textureX, textureY, border, border);
        /* Top          */ texturedModalRect(graphics, texture, x, y - border, width, border, textureX + border, textureY, 1, border);
        /* Top-Right    */ texturedModalRect(graphics, texture, x + width, y - border, textureX + border + 1, textureY, border, border);
        /* Right        */ texturedModalRect(graphics, texture, x + width, y, border, height, textureX + border + 1, textureY + border, border, 1);
        /* Bottom-Right */ texturedModalRect(graphics, texture, x + width, y + height, textureX + border + 1, textureY + border + 1, border, border);
        /* Bottom       */ texturedModalRect(graphics, texture, x, y + height, width, border, textureX + border, textureY + border + 1, 1, border);
        /* Bottom-Left  */ texturedModalRect(graphics, texture, x - border, y + height, textureX, textureY + border + 1, border, border);
        /* Left         */ texturedModalRect(graphics, texture, x - border, y, border, height, textureX, textureY + border, border, 1);
        /* Inside       */ texturedModalRect(graphics, texture, x, y, width, height, textureX + border, textureY + border, 1, 1);
    }

    /** A rectangle of one colour ({@code 0xAARRGGBB}). */
    public static void rectangle(GuiGraphics graphics, int left, int top, int width, int height, int color) {
        graphics.fill(left, top, left + width, top + height, color);
    }

    /** A rectangle shading from {@code colorTop} to {@code colorBottom}. */
    public static void rectangleVerticalGradient(GuiGraphics graphics, int left, int top, int width, int height, int colorTop, int colorBottom) {
        graphics.fillGradient(left, top, left + width, top + height, colorTop, colorBottom);
    }

    /** Draws a widget its owner places and draws itself (not one of the screen's). */
    public static void widget(GuiGraphics graphics, AbstractWidget widget, int mouseX, int mouseY, float partialTicks) {
        //? if >=26.1 {
        /*widget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        *///?} else {
        widget.render(graphics, mouseX, mouseY, partialTicks);
        //?}
    }

    /** Shows the mouse as a pointing hand, over something to click, as 26.x's buttons do (earlier versions keep the arrow). */
    public static void pointer(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.requestCursor(CursorTypes.POINTING_HAND);
        *///?}
    }

    /** Keeps the pose, to be put back by {@link #popPose}. */
    public static void pushPose(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.pose().pushMatrix();
        *///?} else {
        graphics.pose().pushPose();
        //?}
    }

    public static void popPose(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.pose().popMatrix();
        *///?} else {
        graphics.pose().popPose();
        //?}
    }

    public static void translate(GuiGraphics graphics, float x, float y) {
        //? if >=26.1 {
        /*graphics.pose().translate(x, y);
        *///?} else {
        graphics.pose().translate(x, y, 0F);
        //?}
    }

    public static void scale(GuiGraphics graphics, float scale) {
        //? if >=26.1 {
        /*graphics.pose().scale(scale, scale);
        *///?} else {
        graphics.pose().scale(scale, scale, 1F);
        //?}
    }
}
