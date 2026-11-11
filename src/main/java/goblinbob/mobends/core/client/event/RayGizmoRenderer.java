package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.kumo.RayGizmos;
//? if >=26.1 {
/*import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
*///?} else {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//? if >=1.21 {
import com.mojang.blaze3d.vertex.MeshData;
//?}
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
//? if >=1.21 {
import org.joml.Matrix4fStack;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
//?}

/**
 * Draws the rays {@code core:raycast} and {@code core:raycast_array} cast with {@code "debug": true}
 * (see {@link RayGizmos}), over everything, so the ones inside a block or the model still show: the
 * hit kept green, with a short tick along the surface's normal and a cross where it hit, other hits
 * yellow, hits on a surface facing the wrong way red, misses grey. After the world is drawn
 * ({@code LevelRendererMixin}); on 26.x as vanilla's gizmos, always on top, handed over with the
 * frame's others ({@code LevelExtractorMixin}).
 */
public final class RayGizmoRenderer {
    /** How long the normal's tick is, in blocks. */
    private static final double NORMAL_LENGTH = 0.25;
    /** Half the size of the cross on a hit, in blocks. */
    private static final double CROSS_SIZE = 0.05;

    private RayGizmoRenderer() {
    }

    //? if >=26.1 {
    /*/^* Adds this frame's rays to vanilla's gizmos, in the world, to be drawn with them; call with a gizmo collector open. ^/
    public static void emit() {
        forEachLine(0.0, 0.0, 0.0, (x0, y0, z0, x1, y1, z1, r, g, b, a) ->
            Gizmos.line(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1), argb(r, g, b, a), 2.0F).setAlwaysOnTop());
    }

    private static int argb(float r, float g, float b, float a) {
        return Math.round(a * 255.0F) << 24 | Math.round(r * 255.0F) << 16 | Math.round(g * 255.0F) << 8 | Math.round(b * 255.0F);
    }
    *///?} else {
    /**
     * @param camera     where the world was drawn from
     * @param viewMatrix the camera's turn, which the world was drawn with
     */
    public static void render(Camera camera, Matrix4f viewMatrix) {
        if (RayGizmos.count() == 0) {
            return;
        }
        Vec3 view = camera.getPosition();

        //? if >=1.21 {
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(viewMatrix);
        //?} else {
        /*PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(viewMatrix);
        *///?}
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0F);

        //? if >=1.21 {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        //?} else {
        /*BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        *///?}
        forEachLine(view.x, view.y, view.z, (x0, y0, z0, x1, y1, z1, r, g, b, a) -> {
            //? if >=1.21 {
            buffer.addVertex((float) x0, (float) y0, (float) z0).setColor(r, g, b, a);
            buffer.addVertex((float) x1, (float) y1, (float) z1).setColor(r, g, b, a);
            //?} else {
            /*buffer.vertex(x0, y0, z0).color(r, g, b, a).endVertex();
            buffer.vertex(x1, y1, z1).color(r, g, b, a).endVertex();
            *///?}
        });
        //? if >=1.21 {
        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }
        //?} else {
        /*BufferUploader.drawWithShader(buffer.end());
        *///?}

        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        //? if >=1.21 {
        modelView.popMatrix();
        //?} else {
        /*modelView.popPose();
        *///?}
        RenderSystem.applyModelViewMatrix();
    }
    //?}

    /** Each line of this frame's rays, from {@code (originX, originY, originZ)} in the world. */
    private static void forEachLine(double originX, double originY, double originZ, LineVisitor visitor) {
        int count = RayGizmos.count();
        for (int i = 0; i < count; i++) {
            RayGizmos.Kind kind = RayGizmos.kind(i);
            float r, g, b, a;
            switch (kind) {
                case KEPT -> { r = 0.2F; g = 1F; b = 0.2F; a = 1F; }
                case HIT -> { r = 1F; g = 0.9F; b = 0.1F; a = 0.8F; }
                case WRONG_SURFACE -> { r = 1F; g = 0.2F; b = 0.2F; a = 0.8F; }
                default -> { r = 0.7F; g = 0.7F; b = 0.7F; a = 0.35F; }
            }
            double toX = RayGizmos.get(i, 3) - originX, toY = RayGizmos.get(i, 4) - originY, toZ = RayGizmos.get(i, 5) - originZ;
            visitor.line(RayGizmos.get(i, 0) - originX, RayGizmos.get(i, 1) - originY, RayGizmos.get(i, 2) - originZ, toX, toY, toZ, r, g, b, a);
            if (kind == RayGizmos.Kind.MISS) {
                continue;
            }
            visitor.line(toX - CROSS_SIZE, toY, toZ, toX + CROSS_SIZE, toY, toZ, r, g, b, a);
            visitor.line(toX, toY - CROSS_SIZE, toZ, toX, toY + CROSS_SIZE, toZ, r, g, b, a);
            visitor.line(toX, toY, toZ - CROSS_SIZE, toX, toY, toZ + CROSS_SIZE, r, g, b, a);
            if (kind == RayGizmos.Kind.KEPT) {
                // The normal, cyan, so it reads apart from the ray.
                visitor.line(toX, toY, toZ, toX + RayGizmos.get(i, 6) * NORMAL_LENGTH, toY + RayGizmos.get(i, 7) * NORMAL_LENGTH,
                        toZ + RayGizmos.get(i, 8) * NORMAL_LENGTH, 0.2F, 0.9F, 1F, 1F);
            }
        }
    }

    @FunctionalInterface
    private interface LineVisitor {
        void line(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a);
    }
}
