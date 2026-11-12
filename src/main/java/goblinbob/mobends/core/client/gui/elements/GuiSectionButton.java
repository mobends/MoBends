package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.Draw;
import goblinbob.mobends.core.util.GuiHelper;
import goblinbob.mobends.core.util.UIScissorHelper;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Locale;

/**
 * One of the Mo' Bends menu's sections: a button a little taller than Minecraft's, with its name in bold.
 * Hovering sends a white glint through it, which leaves the section's color behind it and brings
 * in the section's icon on the left.
 */
public class GuiSectionButton
{

    public static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/gui/buttons.png");

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
    private static final float U_SCALE = 1F / 512F;
    private static final float V_SCALE = 1F / 128F;

    protected final Minecraft mc;
    private final String label;
    private int x,
                y;
    private int width = 120;
    private final Color neutralColor = Color.fromHex(NEUTRAL_COLOR);
    private final Color bgColor;
    private SectionIcon icon = null;

    private boolean hover;
    private boolean pressed;
    private float ticksAfterHovered = 0.0F;

    public GuiSectionButton(String label, int bgColor)
    {
        this.mc = Minecraft.getMinecraft();
        this.label = "§l" + label.toUpperCase(Locale.ROOT);
        this.bgColor = Color.fromHex(bgColor);
        this.hover = false;
        this.pressed = false;
    }

    public GuiSectionButton setIcon(int u, int v, int width, int height)
    {
        this.icon = new SectionIcon(u, v, width, height);
        return this;
    }

    public void initGui(int x, int y, int width)
    {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void update(int mouseX, int mouseY)
    {
        boolean nowHover = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y
                && mouseY < this.y + HEIGHT;

        if (nowHover && !this.hover)
        {
            this.onHover();
        }

        this.hover = nowHover;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int event)
    {
        if (this.hover)
        {
            GuiHelper.playButtonSound(mc.getSoundHandler());
            this.pressed = true;
        }

        return this.pressed;
    }

    public void mouseReleased(int mouseX, int mouseY, int event)
    {
        this.pressed = false;
    }

    public void onHover()
    {
        this.ticksAfterHovered = 0F;
    }

    public void display()
    {
        this.ticksAfterHovered += DataUpdateHandler.ticksPerFrame;

        drawBackground(this.neutralColor, x, x + width);

        // Where the glint's front edge is along the button's top: it sweeps from the left edge
        // until the glint has left past the right one.
        float glintFront = x - GLINT_SLANT;
        if (this.hover)
        {
            float t = Math.min(1, this.ticksAfterHovered / GLINT_DURATION);
            t = t * t * (3 - 2 * t);
            glintFront = x + t * (width + GLINT_SLANT + GLINT_WIDTH);

            UIScissorHelper.INSTANCE.push(x, y, width, HEIGHT);
            drawBackground(this.bgColor, x - GLINT_SLANT, glintFront);
            drawGlint(glintFront);
            UIScissorHelper.INSTANCE.pop();
        }
        drawBorder();

        // The icon pops in as the glint passes over it, and makes room for itself by pushing the label.
        float iconPop = 0;
        if (this.hover && icon != null)
        {
            float iconCenterX = x + ICON_PADDING + icon.texWidth / 2;
            float glintMiddle = glintFront - GLINT_SLANT / 2F;
            iconPop = MathHelper.clamp((glintMiddle - iconCenterX) / ICON_POP_DISTANCE, 0, 1);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        if (iconPop > 0)
        {
            drawIcon(iconPop);
        }
        drawLabel(iconPop);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Fills the button in a color, from {@code left} to {@code right} along its top: both edges lean
     * as the glint's do. The bottom rows are a shade darker, like the edge of Minecraft's buttons.
     */
    private void drawBackground(Color color, float left, float right)
    {
        final float shade = 0.7F;
        boolean straight = left <= x && right >= x + width;
        float slant = straight ? 0 : GLINT_SLANT;
        int shadeTop = y + HEIGHT - SHADE_HEIGHT;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1, 1, 1, 1);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        slantedQuad(buffer, left, right, slant, y, shadeTop, color.r, color.g, color.b, color.a, color.a);
        slantedQuad(buffer, left, right, slant, shadeTop, y + HEIGHT, color.r * shade, color.g * shade, color.b * shade, color.a, color.a);
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    /** A black outline, like Minecraft's buttons have, over the background's edges. */
    private void drawBorder()
    {
        GlStateManager.color(0, 0, 0, 1);
        Draw.rectangle(x, y, width, BORDER);
        Draw.rectangle(x, y + HEIGHT - BORDER, width, BORDER);
        Draw.rectangle(x, y + BORDER, BORDER, HEIGHT - 2 * BORDER);
        Draw.rectangle(x + width - BORDER, y + BORDER, BORDER, HEIGHT - 2 * BORDER);
    }

    /** A white band, brightest at its front edge and fading out behind it. */
    private void drawGlint(float front)
    {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1, 1, 1, 1);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        slantedQuad(buffer, front - GLINT_WIDTH, front, GLINT_SLANT, y, y + HEIGHT, 1, 1, 1, 0, GLINT_ALPHA);
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
    }

    /**
     * A quad between two edges leaning back by {@code slant} per {@link #HEIGHT}, given by where they
     * cross the button's top, from row {@code top} to row {@code bottom}. Alpha goes from
     * {@code leftAlpha} to {@code rightAlpha} across it.
     */
    private void slantedQuad(WorldRenderer buffer, float left, float right, float slant, float top, float bottom,
                             float r, float g, float b, float leftAlpha, float rightAlpha)
    {
        float topOffset = slant * (top - y) / HEIGHT;
        float bottomOffset = slant * (bottom - y) / HEIGHT;
        buffer.pos(left - bottomOffset, bottom, 0).color(r, g, b, leftAlpha).endVertex();
        buffer.pos(right - bottomOffset, bottom, 0).color(r, g, b, rightAlpha).endVertex();
        buffer.pos(right - topOffset, top, 0).color(r, g, b, rightAlpha).endVertex();
        buffer.pos(left - topOffset, top, 0).color(r, g, b, leftAlpha).endVertex();
    }

    private void drawIcon(float pop)
    {
        // Grows in, overshooting a little before settling, for a pop.
        float scale = (1 + 0.3F * MathHelper.sin(pop * (float) Math.PI)) * Math.min(1, pop * 2);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BUTTONS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + ICON_PADDING + icon.texWidth / 2, y + (HEIGHT - SHADE_HEIGHT + BORDER) / 2, 0);
        GlStateManager.scale(scale, scale, 1);
        icon.draw();
        GlStateManager.popMatrix();
    }

    /** The label, centered in the button, or once the icon is in, in the room to its right. */
    private void drawLabel(float iconPop)
    {
        float eased = iconPop * iconPop * (3 - 2 * iconPop);
        float iconRoom = icon == null ? 0 : (ICON_PADDING + icon.texWidth) * eased;
        float left = x + iconRoom;
        float maxWidth = width - iconRoom - 2 * LABEL_PADDING;

        int textWidth = mc.fontRenderer.getStringWidth(this.label);
        float scale = Math.min(1, maxWidth / textWidth);
        float centerX = left + (x + width - left) / 2F;

        int textY = y + (HEIGHT - SHADE_HEIGHT + BORDER) / 2 - 4;
        if (scale >= 1)
        {
            // On whole pixels: off them, the font's letters (drawn twice a pixel apart, in bold)
            // show seams.
            mc.fontRenderer.drawStringWithShadow(this.label, Math.round(centerX - textWidth / 2F), textY, 0xFFFFFF);
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(Math.round(centerX), textY + 4, 0);
        GlStateManager.scale(scale, scale, 1);
        mc.fontRenderer.drawStringWithShadow(this.label, -textWidth / 2, -4, 0xFFFFFF);
        GlStateManager.popMatrix();
    }

    private static class SectionIcon
    {

        private final int texU;
        private final int texV;
        private final int texWidth;
        private final int texHeight;

        public SectionIcon(int u, int v, int width, int height)
        {
            this.texU = u;
            this.texV = v;
            this.texWidth = width;
            this.texHeight = height;
        }

        /** Draws the icon centered on the origin (to the whole pixel), at its size in pixels. */
        public void draw()
        {
            Draw.texturedRectangle(-texWidth / 2, -texHeight / 2, texWidth, texHeight,
                    texU * U_SCALE, texV * V_SCALE,
                    texWidth * U_SCALE, texHeight * V_SCALE);
        }

    }

}
