package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.Locale;

/**
 * One of the Mo' Bends menu's sections: a button a little taller than Minecraft's, with its name in bold.
 * Hovering sends a white glint through it, which leaves the section's color behind it and brings
 * in the section's icon on the left.
 */
public class GuiSectionButton {
    public static final ResourceLocation BUTTONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoBends.MOD_ID, "textures/gui/buttons.png");

    public static final int HEIGHT = 24;

    /** How long the glint takes to pass through the button, in ticks. */
    private static final float GLINT_DURATION = 6.0F;
    /** How far the glint's edges lean: their bottom is this far behind their top. */
    private static final float GLINT_SLANT = 8.0F;
    private static final float GLINT_WIDTH = 10.0F;
    private static final float GLINT_ALPHA = 0.85F;
    /** How far past the icon's center the glint is when the icon has fully popped in. */
    private static final float ICON_POP_DISTANCE = 14.0F;
    /** The darker rows along the bottom, the lowest of them under the border. */
    private static final int SHADE_HEIGHT = 3;
    private static final int BORDER = 1;
    private static final int ICON_PADDING = 4;
    private static final int LABEL_PADDING = 4;
    private static final int NEUTRAL_COLOR = 0xFF6B6B6B;
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 128;

    protected final Minecraft mc;
    private final Component label;
    private int x;
    private int y;
    private int width = 120;
    private final int bgColor;
    private SectionIcon icon = null;

    private boolean hover;
    private float ticksAfterHovered = 0.0F;
    /** When it was last drawn, to time the glint by (the game's ticks stand still on the title screen). */
    private long lastDrawMillis = -1;

    public GuiSectionButton(Component label, int bgColor) {
        this.mc = Minecraft.getInstance();
        MutableComponent bold = Component.literal(label.getString().toUpperCase(Locale.ROOT));
        this.label = bold.withStyle(style -> style.withBold(true));
        this.bgColor = bgColor;
    }

    public GuiSectionButton setIcon(int u, int v, int width, int height) {
        this.icon = new SectionIcon(u, v, width, height);
        return this;
    }

    public void initGui(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    private void updateHover(int mouseX, int mouseY) {
        boolean nowHover = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + HEIGHT;
        if (nowHover && !this.hover) {
            this.ticksAfterHovered = 0F;
        }
        this.hover = nowHover;
    }

    /** Whether a left click at the mouse is on it; it plays the button's sound if so. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = button == 0 && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + HEIGHT;
        if (clicked) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        return clicked;
    }

    public void display(GuiGraphics graphics, int mouseX, int mouseY) {
        updateHover(mouseX, mouseY);
        long now = Util.getMillis();
        if (lastDrawMillis >= 0) {
            this.ticksAfterHovered += Math.min(now - lastDrawMillis, 1000L) / 50F;
        }
        lastDrawMillis = now;

        drawBackground(graphics, NEUTRAL_COLOR, x, x + width);

        // Where the glint's front edge is along the button's top: it sweeps from the left edge
        // until the glint has left past the right one.
        float glintFront = x - GLINT_SLANT;
        if (this.hover) {
            float t = Math.min(1, this.ticksAfterHovered / GLINT_DURATION);
            t = t * t * (3 - 2 * t);
            glintFront = x + t * (width + GLINT_SLANT + GLINT_WIDTH);

            graphics.enableScissor(x, y, x + width, y + HEIGHT);
            drawBackground(graphics, bgColor, x - GLINT_SLANT, glintFront);
            drawGlint(graphics, glintFront);
            graphics.disableScissor();
        }
        drawBorder(graphics);

        // The icon pops in as the glint passes over it, and makes room for itself by pushing the label.
        float iconPop = 0;
        if (this.hover && icon != null) {
            float iconCenterX = x + ICON_PADDING + icon.texWidth / 2;
            float glintMiddle = glintFront - GLINT_SLANT / 2F;
            iconPop = Mth.clamp((glintMiddle - iconCenterX) / ICON_POP_DISTANCE, 0, 1);
        }

        if (iconPop > 0) {
            drawIcon(graphics, iconPop);
        }
        drawLabel(graphics, iconPop);
    }

    /**
     * Fills the button in a color, from {@code left} to {@code right} along its top: both edges lean
     * as the glint's do. The bottom rows are a shade darker, like the edge of Minecraft's buttons.
     */
    private void drawBackground(GuiGraphics graphics, int color, float left, float right) {
        final float shade = 0.7F;
        boolean straight = left <= x && right >= x + width;
        float slant = straight ? 0 : GLINT_SLANT;
        int shadeTop = y + HEIGHT - SHADE_HEIGHT;
        float a = (color >>> 24) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8 & 255) / 255F;
        float b = (color & 255) / 255F;

        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f pose = graphics.pose().last().pose();
        slantedQuad(buffer, pose, left, right, slant, y, shadeTop, r, g, b, a, a);
        slantedQuad(buffer, pose, left, right, slant, shadeTop, y + HEIGHT, r * shade, g * shade, b * shade, a, a);
        graphics.flush();
    }

    /** A black outline, like Minecraft's buttons have, over the background's edges. */
    private void drawBorder(GuiGraphics graphics) {
        Draw.rectangle(graphics, x, y, width, BORDER, 0xFF000000);
        Draw.rectangle(graphics, x, y + HEIGHT - BORDER, width, BORDER, 0xFF000000);
        Draw.rectangle(graphics, x, y + BORDER, BORDER, HEIGHT - 2 * BORDER, 0xFF000000);
        Draw.rectangle(graphics, x + width - BORDER, y + BORDER, BORDER, HEIGHT - 2 * BORDER, 0xFF000000);
    }

    /** A white band, brightest at its front edge and fading out behind it. */
    private void drawGlint(GuiGraphics graphics, float front) {
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        slantedQuad(buffer, graphics.pose().last().pose(), front - GLINT_WIDTH, front, GLINT_SLANT, y, y + HEIGHT, 1, 1, 1, 0, GLINT_ALPHA);
        graphics.flush();
    }

    /**
     * A quad between two edges leaning back by {@code slant} per {@link #HEIGHT}, given by where they
     * cross the button's top, from row {@code top} to row {@code bottom}. Alpha goes from
     * {@code leftAlpha} to {@code rightAlpha} across it.
     */
    private void slantedQuad(VertexConsumer buffer, Matrix4f pose, float left, float right, float slant, float top, float bottom,
                             float r, float g, float b, float leftAlpha, float rightAlpha) {
        float topOffset = slant * (top - y) / HEIGHT;
        float bottomOffset = slant * (bottom - y) / HEIGHT;
        buffer.addVertex(pose, left - topOffset, top, 0).setColor(r, g, b, leftAlpha);
        buffer.addVertex(pose, left - bottomOffset, bottom, 0).setColor(r, g, b, leftAlpha);
        buffer.addVertex(pose, right - bottomOffset, bottom, 0).setColor(r, g, b, rightAlpha);
        buffer.addVertex(pose, right - topOffset, top, 0).setColor(r, g, b, rightAlpha);
    }

    private void drawIcon(GuiGraphics graphics, float pop) {
        // Grows in, overshooting a little before settling, for a pop.
        float scale = (1 + 0.3F * Mth.sin(pop * (float) Math.PI)) * Math.min(1, pop * 2);

        graphics.pose().pushPose();
        graphics.pose().translate(x + ICON_PADDING + icon.texWidth / 2, y + (HEIGHT - SHADE_HEIGHT + BORDER) / 2, 0);
        graphics.pose().scale(scale, scale, 1);
        icon.draw(graphics);
        graphics.pose().popPose();
    }

    /** The label, centered in the button, or once the icon is in, in the room to its right. */
    private void drawLabel(GuiGraphics graphics, float iconPop) {
        Font font = mc.font;
        float eased = iconPop * iconPop * (3 - 2 * iconPop);
        float iconRoom = icon == null ? 0 : (ICON_PADDING + icon.texWidth) * eased;
        float left = x + iconRoom;
        float maxWidth = width - iconRoom - 2 * LABEL_PADDING;

        int textWidth = font.width(this.label);
        float scale = Math.min(1, maxWidth / textWidth);
        float centerX = left + (x + width - left) / 2F;

        int textY = y + (HEIGHT - SHADE_HEIGHT + BORDER) / 2 - 4;
        if (scale >= 1) {
            // On whole pixels: off them, the font's letters (drawn twice a pixel apart, in bold) show seams.
            graphics.drawString(font, this.label, Math.round(centerX - textWidth / 2F), textY, 0xFFFFFF);
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(Math.round(centerX), textY + 4, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, this.label, -textWidth / 2, -4, 0xFFFFFF);
        graphics.pose().popPose();
    }

    private static class SectionIcon {
        private final int texU;
        private final int texV;
        private final int texWidth;
        private final int texHeight;

        SectionIcon(int u, int v, int width, int height) {
            this.texU = u;
            this.texV = v;
            this.texWidth = width;
            this.texHeight = height;
        }

        /** Draws the icon centered on the origin (to the whole pixel), at its size in pixels. */
        void draw(GuiGraphics graphics) {
            graphics.blit(BUTTONS_TEXTURE, -texWidth / 2, -texHeight / 2, texWidth, texHeight, texU, texV, texWidth, texHeight,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}
