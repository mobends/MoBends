package goblinbob.mobends.core.util;

// Only the Mo' Bends menu uses it, which isn't ported to 26.x's GUI yet.
//? if <26.1 {
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** 1.12's drawing helpers the Mo' Bends menu uses, on a {@link GuiGraphics}. Textures are 256×256 pixels, drawn blended. */
public final class Draw {
    private static final int TEXTURE_SIZE = 256;

    private Draw() {
    }

    /** A part of the texture, drawn at its size. */
    public static void texturedModalRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int textureX, int textureY, int width, int height) {
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, textureX, textureY, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    /** A part of the texture, stretched to {@code width} × {@code height}. */
    public static void texturedModalRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                         int textureX, int textureY, int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, width, height, textureX, textureY, textureWidth, textureHeight, TEXTURE_SIZE, TEXTURE_SIZE);
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
}
//?}
