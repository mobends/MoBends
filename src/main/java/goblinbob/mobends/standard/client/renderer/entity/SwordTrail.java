package goblinbob.mobends.standard.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.BendsLayers;
import goblinbob.mobends.core.client.definition.DefinedModel;
import goblinbob.mobends.core.configuration.ClientConfig;
import goblinbob.mobends.core.data.EntityComponent;
import goblinbob.mobends.core.definition.DefinedEntityData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@code mobends:sword_trail}: the streak a swung sword leaves, fed by the {@code mobends:sword_trail}
 * driver and drawn in the entity's frame (its feet, turned as its body is), so it turns with the
 * body as 1.12's did.
 * <p>
 * 1.12 noted the bones' transforms when the driver asked, and worked out the hand from them as it
 * drew. Here the driver only asks ({@link #add}), and the hand is noted as the bent model is drawn
 * ({@link #capture}): in the frame the main hand's item is drawn in, with everything vanilla and the
 * root and center bones do to the model, so the trail follows the sword however the entity is turned.
 */
public class SwordTrail implements EntityComponent {
    /** How long a part of the trail takes to fade, and how long it is kept, in ticks. */
    private static final float FADE_TICKS = 5.0F;
    private static final float LIFE_TICKS = 20.0F;
    /** The trail's colour; supporters' own colours come with the supporters. It's drawn unless the config says not to. */
    private static final float RED = 1.0F, GREEN = 1.0F, BLUE = 1.0F;

    private final List<TrailPart> parts = new ArrayList<>();
    /** The velocity of the part the driver asked for this frame (pixels per tick, in the entity's frame), or null. */
    private Vector3f pending;

    /** A part of the trail: where the blade was, in the entity's frame. */
    private static final class TrailPart {
        /** The hand's frame (as {@link BendsLayers#toHand} leaves it) in the entity's frame. */
        final Matrix4f hand;
        /** Which side the hand is on: -1 for the right, 1 for the left (where the item is held, in pixels). */
        final float side;
        final Vector3f velocity;
        /** How far it has moved since, in pixels. */
        final Vector3f offset = new Vector3f();
        float ticksExisted;

        TrailPart(Matrix4f hand, float side, Vector3f velocity) {
            this.hand = hand;
            this.side = side;
            this.velocity = velocity;
        }

        void update(float ticksPerFrame) {
            ticksExisted += ticksPerFrame;
            offset.add(velocity.x * ticksPerFrame, velocity.y * ticksPerFrame, velocity.z * ticksPerFrame);
        }

        float alpha() {
            return 1.0F - Math.min(ticksExisted / FADE_TICKS, 1.0F);
        }

        /**
         * The blade's two ends, in the entity's frame: from the hand (ten pixels down the arm, a
         * pixel to the side) straight ahead along the forearm, a block long, shrinking to its
         * middle as it fades.
         */
        void ends(Vector3f near, Vector3f far) {
            float alpha = alpha();
            near.set(side / 16.0F, 10.0F / 16.0F, -(8.0F - 8.0F * alpha) / 16.0F);
            far.set(side / 16.0F, 10.0F / 16.0F, -(8.0F + 8.0F * alpha) / 16.0F);
            hand.transformPosition(near);
            hand.transformPosition(far);
            // The entity's frame has Y up and Z ahead, in blocks; the offset is in pixels.
            near.add(offset.x / 16.0F, offset.y / 16.0F, offset.z / 16.0F);
            far.add(offset.x / 16.0F, offset.y / 16.0F, offset.z / 16.0F);
        }
    }

    public void reset() {
        parts.clear();
        pending = null;
    }

    /**
     * Asks for the main hand to be noted where it is this frame, moving by {@code velocity} (pixels
     * per tick, in the entity's frame: Y up, Z ahead) as it fades. It's noted when the bent model is
     * next drawn ({@link #capture}).
     */
    public void add(float velocityX, float velocityY, float velocityZ) {
        pending = new Vector3f(velocityX, velocityY, velocityZ);
    }

    /**
     * The entity's frame: its feet, turned as its body is, Y up, in blocks (1.12's frame for the
     * root and center bones, see {@code DefinedRenderer}).
     *
     * @param entityFrame the pose as vanilla's {@code setupRotations} starts
     * @param bodyYaw     the body yaw vanilla turns the entity by
     */
    private static Matrix4f entityFrame(Matrix4fc entityFrame, float bodyYaw) {
        return new Matrix4f(entityFrame).rotateY((float) Math.toRadians(-bodyYaw));
    }

    /**
     * As the bent model is drawn ({@code BendsRenderer}), with {@code poseStack} in the space the
     * bent parts are drawn in: notes the main hand, if the driver asked for it this frame. An entity
     * without a bone for that arm leaves no trail.
     */
    public static void capture(DefinedEntityData<?> data, EntityModel<?> model, DefinedModel parts, Matrix4fc entityFrame, float bodyYaw,
                               PoseStack poseStack) {
        SwordTrail trail = data.getComponent("swordTrail", SwordTrail.class);
        if (trail == null || trail.pending == null) {
            return;
        }
        Vector3f velocity = trail.pending;
        trail.pending = null;
        HumanoidArm arm = data.getEntity().getMainArm();
        String bone = BendsLayers.armBone(data.getDefinition(), arm);
        if (!parts.hasPart(bone)) {
            return;
        }
        poseStack.pushPose();
        DefinedModel.BabyLayout baby = data.getEntity().isBaby() ? parts.babyLayout() : null;
        if (baby != null) {
            baby.apply(parts.rootPartOf(bone), poseStack);
        }
        BendsLayers.toHand(data, model, parts, arm, bone, poseStack);
        Matrix4f hand = entityFrame(entityFrame, bodyYaw).invertAffine().mul(poseStack.last().pose());
        poseStack.popPose();
        trail.parts.add(new TrailPart(hand, arm == HumanoidArm.RIGHT ? -1.0F : 1.0F, velocity));
    }

    /** After the entity's render ({@code LivingEntityRendererMixin}): draws its trail, if it has one. */
    public static void draw(DefinedEntityData<?> data, Matrix4fc entityFrame, float bodyYaw, MultiBufferSource buffers) {
        SwordTrail trail = data.getComponent("swordTrail", SwordTrail.class);
        if (trail == null || trail.parts.size() < 2 || !ClientConfig.INSTANCE.showSwordTrail()) {
            return;
        }
        Matrix4f frame = entityFrame(entityFrame, bodyYaw);
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        Vector3f near = new Vector3f(), far = new Vector3f(), lastNear = new Vector3f(), lastFar = new Vector3f();
        float lastAlpha = 0.0F;
        for (int i = 0; i < trail.parts.size(); i++) {
            TrailPart part = trail.parts.get(i);
            part.ends(near, far);
            float alpha = part.alpha();
            if (i > 0) {
                // A quad from the last part to this one, each end as faded as its part.
                consumer.addVertex(frame, lastNear.x, lastNear.y, lastNear.z).setColor(RED, GREEN, BLUE, lastAlpha);
                consumer.addVertex(frame, lastFar.x, lastFar.y, lastFar.z).setColor(RED, GREEN, BLUE, lastAlpha);
                consumer.addVertex(frame, far.x, far.y, far.z).setColor(RED, GREEN, BLUE, alpha);
                consumer.addVertex(frame, near.x, near.y, near.z).setColor(RED, GREEN, BLUE, alpha);
            }
            lastNear.set(near);
            lastFar.set(far);
            lastAlpha = alpha;
        }
    }

    @Override
    public void update(float ticksPerFrame) {
        Iterator<TrailPart> it = parts.iterator();
        while (it.hasNext()) {
            TrailPart part = it.next();
            part.update(ticksPerFrame);
            if (part.ticksExisted > LIFE_TICKS) {
                it.remove();
            }
        }
    }
}
