package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.configuration.ClientConfig;
import net.minecraft.client.Minecraft;
//? if <26.1 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The streak a flying arrow leaves, as 1.12 drew it: the arrow's last {@link #MAX_LENGTH} places, a
 * tick apart, joined by a thin white cross that narrows to nothing at the tail. Drawn with the arrow
 * ({@code ArrowRendererMixin}) unless the config switches it off ({@code showArrowTrails}).
 */
public final class ArrowTrail {
    public static final int MAX_LENGTH = 10;
    /** How often the trail takes the arrow's place, in ticks. */
    public static final float SPAWN_INTERVAL = 1;

    /** By arrow; render thread only. */
    private static final Map<AbstractArrow, ArrowTrail> TRAILS = new WeakHashMap<>();

    private final AbstractArrow arrow;
    private final Node[] nodes = new Node[MAX_LENGTH];
    private float spawnCooldown = SPAWN_INTERVAL;

    private ArrowTrail(AbstractArrow arrow) {
        this.arrow = arrow;
        resetNodes();
    }

    /** At the start of every frame ({@code DataUpdateHandler}): the trails move on, and those of arrows gone are dropped. */
    public static void onRenderFrame() {
        if (Minecraft.getInstance().level == null) {
            TRAILS.clear();
            return;
        }
        Iterator<ArrowTrail> it = TRAILS.values().iterator();
        while (it.hasNext()) {
            ArrowTrail trail = it.next();
            if (trail.arrow.isRemoved()) {
                it.remove();
            } else {
                trail.spawnCooldown += DataUpdateHandler.ticksPerFrame;
            }
        }
    }

    // Not drawn on 26.x yet.
    //? if <26.1 {
    /** As {@code arrow} is drawn: its trail, in the frame the arrow is drawn in (at its place, unturned). */
    public static void draw(AbstractArrow arrow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers) {
        if (!ClientConfig.INSTANCE.showArrowTrails()) {
            return;
        }
        TRAILS.computeIfAbsent(arrow, ArrowTrail::new).draw(partialTicks, poseStack, buffers);
    }
    //?}

    private void resetNodes() {
        for (int i = 0; i < MAX_LENGTH; i++) {
            nodes[i] = new Node();
            nodes[i].moveTo(arrow);
        }
    }

    // Not drawn on 26.x yet.
    //? if <26.1 {
    private void draw(float partialTicks, PoseStack poseStack, MultiBufferSource buffers) {
        if (spawnCooldown > 40) {
            // Not drawn for a while (out of sight): starting over.
            spawnCooldown = 0;
            resetNodes();
        }
        while (spawnCooldown >= SPAWN_INTERVAL) {
            for (int i = MAX_LENGTH - 1; i > 0; i--) {
                nodes[i].moveTo(nodes[i - 1]);
            }
            nodes[0].moveTo(arrow);
            spawnCooldown -= SPAWN_INTERVAL;
        }

        // The arrow is drawn at its interpolated place.
        double ox = Mth.lerp(partialTicks, arrow.xo, arrow.getX());
        double oy = Mth.lerp(partialTicks, arrow.yo, arrow.getY());
        double oz = Mth.lerp(partialTicks, arrow.zo, arrow.getZ());
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        for (int i = 1; i < MAX_LENGTH; i++) {
            Node node0 = nodes[i - 1];
            Node node1 = nodes[i];
            float scale0 = (float) (MAX_LENGTH - i) / MAX_LENGTH * 0.1F;
            float scale1 = i == 1 ? 0 : (MAX_LENGTH - i - 1.0F) / MAX_LENGTH * 0.1F;
            float x0 = (float) (node0.x - ox), y0 = (float) (node0.y - oy), z0 = (float) (node0.z - oz);
            float x1 = (float) (node1.x - ox), y1 = (float) (node1.y - oy), z1 = (float) (node1.z - oz);
            quad(consumer, pose, x0, y0, z0, node0.right, scale0, x1, y1, z1, node1.right, scale1);
            quad(consumer, pose, x0, y0, z0, node0.up, scale0, x1, y1, z1, node1.up, scale1);
        }
    }
    //?}

    private static void quad(VertexConsumer consumer, Matrix4f pose, float x0, float y0, float z0, Vector3f side0, float scale0,
                             float x1, float y1, float z1, Vector3f side1, float scale1) {
        vertex(consumer, pose, x0 - side0.x * scale0, y0 - side0.y * scale0, z0 - side0.z * scale0);
        vertex(consumer, pose, x0 + side0.x * scale0, y0 + side0.y * scale0, z0 + side0.z * scale0);
        vertex(consumer, pose, x1 + side1.x * scale1, y1 + side1.y * scale1, z1 + side1.z * scale1);
        vertex(consumer, pose, x1 - side1.x * scale1, y1 - side1.y * scale1, z1 - side1.z * scale1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z) {
        VanillaDraw.vertex(consumer, pose, x, y, z, 1.0F, 1.0F, 1.0F, 0.5F);
    }

    /** A place the arrow was, and two directions across its flight there, at right angles. */
    private static final class Node {
        double x, y, z;
        final Vector3f up = new Vector3f(0, 1, 0);
        final Vector3f right = new Vector3f(1, 0, 0);

        void moveTo(Node node) {
            x = node.x;
            y = node.y;
            z = node.z;
            up.set(node.up);
            right.set(node.right);
        }

        void moveTo(AbstractArrow arrow) {
            x = arrow.getX();
            y = arrow.getY();
            z = arrow.getZ();
            Vec3 motion = arrow.getDeltaMovement();
            if (motion.lengthSqr() < 1.0E-6) {
                // Stuck: keeps the directions it flew with.
                return;
            }
            Vector3f forward = new Vector3f((float) motion.x, (float) motion.y, (float) motion.z).normalize();
            // Across the flight, level with the ground (or along X, for an arrow flying straight up or down).
            Vector3f side = new Vector3f(forward).cross(0, 1, 0);
            if (side.lengthSquared() < 1.0E-6F) {
                side.set(1, 0, 0);
            }
            right.set(side.normalize());
            up.set(new Vector3f(right).cross(forward).normalize());
        }
    }
}
