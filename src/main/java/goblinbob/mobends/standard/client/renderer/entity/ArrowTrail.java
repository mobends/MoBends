package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.configuration.ClientConfig;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?} else {
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
//?}
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The streak a flying arrow leaves, as 1.12 drew it: the arrow's last {@link #MAX_LENGTH} places, a
 * tick apart, joined by a thin white cross that narrows to nothing at the tail. Drawn with the arrow
 * ({@code ArrowRendererMixin}) unless the config switches it off ({@code showArrowTrails}).
 */
public final class ArrowTrail {
    /** An arrow's render state on 26.x, which carries its trail's quads from the arrow to where it's submitted ({@code ArrowRenderStateMixin}). */
    public interface State {
        @Nullable
        float[] mobends$trail();

        void mobends$setTrail(@Nullable float[] vertices);
    }

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

    /**
     * As {@code arrow} is drawn (26.x: as its render state is filled, {@code ArrowRendererMixin}): moves
     * its trail on, and gives its quads in the frame the arrow is drawn in (at its place, unturned),
     * three numbers a corner; null if the config hides the trails.
     */
    @Nullable
    public static float[] advance(AbstractArrow arrow, float partialTicks) {
        if (!ClientConfig.INSTANCE.showArrowTrails()) {
            return null;
        }
        return TRAILS.computeIfAbsent(arrow, ArrowTrail::new).advance(partialTicks);
    }

    //? if >=26.1 {
    /*/^* Submits the quads {@link #advance} gave, in the frame the arrow is drawn in, to be drawn later in the frame. ^/
    public static void submit(@Nullable float[] vertices, PoseStack poseStack, SubmitNodeCollector collector) {
        if (vertices != null) {
            collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, consumer) -> emit(consumer, pose.pose(), vertices));
        }
    }
    *///?} else {
    /** As {@code arrow} is drawn: its trail, in the frame the arrow is drawn in (at its place, unturned). */
    public static void draw(AbstractArrow arrow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers) {
        float[] vertices = advance(arrow, partialTicks);
        if (vertices != null) {
            emit(buffers.getBuffer(RenderType.debugQuads()), poseStack.last().pose(), vertices);
        }
    }
    //?}

    private void resetNodes() {
        for (int i = 0; i < MAX_LENGTH; i++) {
            nodes[i] = new Node();
            nodes[i].moveTo(arrow);
        }
    }

    private float[] advance(float partialTicks) {
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
        float[] vertices = new float[(MAX_LENGTH - 1) * 2 * 12];
        int at = 0;
        for (int i = 1; i < MAX_LENGTH; i++) {
            Node node0 = nodes[i - 1];
            Node node1 = nodes[i];
            float scale0 = (float) (MAX_LENGTH - i) / MAX_LENGTH * 0.1F;
            float scale1 = i == 1 ? 0 : (MAX_LENGTH - i - 1.0F) / MAX_LENGTH * 0.1F;
            float x0 = (float) (node0.x - ox), y0 = (float) (node0.y - oy), z0 = (float) (node0.z - oz);
            float x1 = (float) (node1.x - ox), y1 = (float) (node1.y - oy), z1 = (float) (node1.z - oz);
            at = quad(vertices, at, x0, y0, z0, node0.right, scale0, x1, y1, z1, node1.right, scale1);
            at = quad(vertices, at, x0, y0, z0, node0.up, scale0, x1, y1, z1, node1.up, scale1);
        }
        return vertices;
    }

    private static int quad(float[] vertices, int at, float x0, float y0, float z0, Vector3f side0, float scale0,
                            float x1, float y1, float z1, Vector3f side1, float scale1) {
        at = corner(vertices, at, x0 - side0.x * scale0, y0 - side0.y * scale0, z0 - side0.z * scale0);
        at = corner(vertices, at, x0 + side0.x * scale0, y0 + side0.y * scale0, z0 + side0.z * scale0);
        at = corner(vertices, at, x1 + side1.x * scale1, y1 + side1.y * scale1, z1 + side1.z * scale1);
        return corner(vertices, at, x1 - side1.x * scale1, y1 - side1.y * scale1, z1 - side1.z * scale1);
    }

    private static int corner(float[] vertices, int at, float x, float y, float z) {
        vertices[at] = x;
        vertices[at + 1] = y;
        vertices[at + 2] = z;
        return at + 3;
    }

    /** Writes the quads {@link #advance} gave, white and half see-through, in {@code pose}. */
    private static void emit(VertexConsumer consumer, Matrix4f pose, float[] vertices) {
        for (int at = 0; at < vertices.length; at += 3) {
            VanillaDraw.vertex(consumer, pose, vertices[at], vertices[at + 1], vertices[at + 2], 1.0F, 1.0F, 1.0F, 0.5F);
        }
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
