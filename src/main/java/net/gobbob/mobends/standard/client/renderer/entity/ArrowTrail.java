package net.gobbob.mobends.standard.client.renderer.entity;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.VectorUtils;
import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ArrowTrail
{

    public static final int MAX_LENGTH = 10;
    public static final float SPAWN_INTERVAL = 1;

    private final Minecraft mc;
    private EntityArrow trackedArrow;
    private TrailNode[] nodes;
    private float spawnCooldown = 0;

    public ArrowTrail(EntityArrow arrow)
    {
        this.mc = Minecraft.getMinecraft();
        this.trackedArrow = arrow;
        this.spawnCooldown = SPAWN_INTERVAL;
        this.nodes = new TrailNode[MAX_LENGTH];

        resetNodes();
    }

    public void onRenderTick()
    {
        spawnCooldown += DataUpdateHandler.ticksPerFrame;
    }

    public void render(double x, double y, double z, float partialTicks)
    {
        if (this.spawnCooldown > 40)
        {
            this.spawnCooldown = 0;
            resetNodes();
        }

        while (this.spawnCooldown >= SPAWN_INTERVAL)
        {
            for (int i = MAX_LENGTH - 1; i > 0; i--)
            {
                nodes[i].moveTo(nodes[i - 1]);
            }
            nodes[0].moveTo(trackedArrow);
            this.spawnCooldown -= SPAWN_INTERVAL;
        }

        renderNodes(partialTicks);
    }

    public void resetNodes()
    {
        for (int i = 0; i < MAX_LENGTH; i++)
            this.nodes[i] = new TrailNode(trackedArrow);
    }

    public void renderNodes(float partialTicks)
    {
        final Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();

        if (viewEntity == null)
            return;
        Vec3d viewPos = new Vec3d(viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * partialTicks,
                viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * partialTicks,
                viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * partialTicks);

        float r = 1;
        float g = 1;
        float b = 1;
        float a = 0.5F;
        GlStateManager.color(r, g, b, a);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int i = 1; i < MAX_LENGTH; i++)
        {
            TrailNode node0 = nodes[i - 1];
            TrailNode node1 = nodes[i];

            Vec3d pos0 = new Vec3d(node0.x - viewPos.x, node0.y - viewPos.y, node0.z - viewPos.z);
            Vec3d pos1 = new Vec3d(node1.x - viewPos.x, node1.y - viewPos.y, node1.z - viewPos.z);
            float scale0 = ((float) (MAX_LENGTH - i)) / MAX_LENGTH * .1F;
            float scale1 = ((float) MAX_LENGTH - i - 1.0f) / MAX_LENGTH * .1F;
            if (i == 1)
            {
                scale1 = 0;
            }
            Vec3f up0 = node0.up, right0 = node0.right, up1 = node1.up, right1 = node1.right;

            vertexbuffer
                    .pos(pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer
                    .pos(pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();

            vertexbuffer.pos(pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
            vertexbuffer.pos(pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1)
                    .tex(0.0D, 0.15625D).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /**
     * Used for debugging.
     * @param x
     * @param y
     * @param z
     */
    public void renderAxis(double x, double y, double z)
    {
        Vec3d forward = trackedArrow.getForward();
        forward = new Vec3d(-forward.x, -forward.y, forward.z);
        Vec3d up = Vec3d.fromPitchYaw(trackedArrow.rotationPitch + 90.0f, trackedArrow.rotationYaw);
        up = new Vec3d(-up.x, -up.y, up.z);
        Vec3d right = forward.crossProduct(up);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthFunc(GL11.GL_ALWAYS);
        GlStateManager.translate(x, y, z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.color(1, 0, 0, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(right.x, right.y, right.z).tex(0.15625D, 0.15625D).endVertex();
        tessellator.draw();
        GlStateManager.color(0, 1, 0, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(up.x, up.y, up.z).tex(0.15625D, 0.15625D).endVertex();
        tessellator.draw();
        GlStateManager.color(0, 0, 1, 1);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(forward.x, forward.y, forward.z).tex(0.15625D, 0.15625D).endVertex();
        tessellator.draw();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public boolean shouldBeRemoved()
    {
        return mc.world == null || trackedArrow.isDead;
    }

    static class TrailNode
    {

        double x;
        double y;
        double z;

        final Vec3f up, right;

        TrailNode(EntityArrow arrow)
        {
            this.up = new Vec3f();
            this.right = new Vec3f();

            this.moveTo(arrow);
        }

        public void moveTo(TrailNode trailNode)
        {
            this.x = trailNode.x;
            this.y = trailNode.y;
            this.z = trailNode.z;
            this.up.set(trailNode.up);
            this.right.set(trailNode.right);
        }

        public void moveTo(EntityArrow arrow)
        {
            this.x = arrow.posX;
            this.y = arrow.posY;
            this.z = arrow.posZ;

            final Vec3d forward = arrow.getForward();
            final Vec3d up = Vec3d.fromPitchYaw(arrow.rotationPitch + 90F, arrow.rotationYaw);

            this.up.set((float) -up.x, (float) -up.y, (float) up.z);

            VectorUtils.cross(
                    (float) -forward.x, (float) -forward.y, (float) forward.z,
                    this.up.x, this.up.y, this.up.z, this.right);
        }

    }

}
