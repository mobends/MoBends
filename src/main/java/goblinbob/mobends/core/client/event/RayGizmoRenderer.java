package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.kumo.RayGizmos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Draws the rays {@code core:raycast} and {@code core:raycast_array} cast with {@code "debug": true}
 * (see {@link RayGizmos}), over everything, so the ones inside a block or the model still show: the
 * hit kept green, with a short tick along the surface's normal and a cross where it hit, other hits
 * yellow, hits on a surface facing the wrong way red, misses grey.
 */
public class RayGizmoRenderer
{

    /** How long the normal's tick is, in blocks. */
    private static final double NORMAL_LENGTH = 0.25;
    /** Half the size of the cross on a hit, in blocks. */
    private static final double CROSS_SIZE = 0.05;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        final int count = RayGizmos.count();
        final Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        if (count == 0 || view == null)
            return;

        final float pt = event.getPartialTicks();
        final double viewX = view.lastTickPosX + (view.posX - view.lastTickPosX) * pt;
        final double viewY = view.lastTickPosY + (view.posY - view.lastTickPosY) * pt;
        final double viewZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * pt;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewX, -viewY, -viewZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.glLineWidth(2F);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < count; i++)
        {
            final RayGizmos.Kind kind = RayGizmos.kind(i);
            final float r, g, b, a;
            switch (kind)
            {
                case KEPT:          r = 0.2F; g = 1F;   b = 0.2F; a = 1F;    break;
                case HIT:           r = 1F;   g = 0.9F; b = 0.1F; a = 0.8F;  break;
                case WRONG_SURFACE: r = 1F;   g = 0.2F; b = 0.2F; a = 0.8F;  break;
                default:            r = 0.7F; g = 0.7F; b = 0.7F; a = 0.35F; break;
            }
            final double toX = RayGizmos.get(i, 3), toY = RayGizmos.get(i, 4), toZ = RayGizmos.get(i, 5);
            line(buffer, RayGizmos.get(i, 0), RayGizmos.get(i, 1), RayGizmos.get(i, 2), toX, toY, toZ, r, g, b, a);
            if (kind == RayGizmos.Kind.MISS)
                continue;

            line(buffer, toX - CROSS_SIZE, toY, toZ, toX + CROSS_SIZE, toY, toZ, r, g, b, a);
            line(buffer, toX, toY - CROSS_SIZE, toZ, toX, toY + CROSS_SIZE, toZ, r, g, b, a);
            line(buffer, toX, toY, toZ - CROSS_SIZE, toX, toY, toZ + CROSS_SIZE, r, g, b, a);
            if (kind == RayGizmos.Kind.KEPT)
            {
                // The normal, cyan, so it reads apart from the ray.
                line(buffer, toX, toY, toZ,
                        toX + RayGizmos.get(i, 6) * NORMAL_LENGTH,
                        toY + RayGizmos.get(i, 7) * NORMAL_LENGTH,
                        toZ + RayGizmos.get(i, 8) * NORMAL_LENGTH,
                        0.2F, 0.9F, 1F, 1F);
            }
        }
        tessellator.draw();

        GlStateManager.glLineWidth(1F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void line(BufferBuilder buffer, double x0, double y0, double z0, double x1, double y1, double z1,
                             float r, float g, float b, float a)
    {
        buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
        buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }

}
