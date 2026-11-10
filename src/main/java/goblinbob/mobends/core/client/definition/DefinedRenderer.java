package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

/**
 * What 1.12's {@code MutatedRenderer.beforeRender} and {@code DefinedRenderer.transformLocally} did
 * to the entity's frame before its model was drawn: the {@code root} bone's move and turn, the
 * {@code center} bone's move and turn about {@link DefinedEntityData#getCenterPivot}, and the
 * definition's sneak offset.
 * <p>
 * 1.12 applied them before vanilla's own transforms, in a frame of its own: at the entity's
 * position, turned by the body's yaw, Y up, in blocks; then it turned back by the yaw, and vanilla
 * went on (turning by {@code 180 - yaw}, then any death, sleep or glide turn, flipping the model
 * upside down with {@code scale(-1, -1, 1)}, scaling it and moving it down by 1.501):
 * <pre>
 *     entity * Ry(-yaw) * [root, center, sneak] * Ry(yaw) * [vanilla's turns, flip, scale, -1.501] * model
 * </pre>
 * Here they are applied as the model is drawn, when the pose stack already holds all of vanilla's
 * transforms. {@code entityFrame} is the pose as vanilla's {@code setupRotations} starts (the entity's
 * position and scale), so vanilla's own part is {@code toModel = entityFrame^-1 * pose}: undoing it
 * gives the entity's frame back, where the bones go as 1.12 put them, between the yaw and its undoing,
 * and then {@code toModel} again. The result is 1.12's matrix, whatever vanilla turns the entity by.
 */
public final class DefinedRenderer {
    private static final float SCALE = 0.0625F;

    private DefinedRenderer() {
    }

    /**
     * Whether vanilla's swimming tilt is kept off the bent entity ({@code mixin.PlayerRendererMixin}):
     * a player in water, where the animator turns the root bone for its own swimming poses. Out of
     * water (crawling) vanilla's tilt stays, as the animator has no crawling pose; and so does a
     * swimming drowned's, which the zombie's animator has no swimming pose for.
     */
    public static boolean replacesSwimmingTilt(LivingEntity entity) {
        return entity instanceof Player && entity.isInWater();
    }

    /**
     * How far down to draw the bent model of an entity in vanilla's swimming pose, in the entity's
     * scale. The animator's swimming poses were made for 1.12, where a swimming player kept the
     * standing hitbox and camera; vanilla's swimming pose shrinks the hitbox to its bottom and lowers
     * the camera with it (1.22 blocks for a player), so the pose drops by as much to keep its place
     * against the camera and the hitbox. Vanilla's own swimming translate went with the tilt it no
     * longer applies. Eased by the swim amount, as vanilla eases its tilt, so it doesn't jump.
     */
    private static float swimmingDrop(LivingEntity entity) {
        if (!replacesSwimmingTilt(entity)) {
            return 0.0F;
        }
        float swimAmount = entity.getSwimAmount(DataUpdateHandler.partialTicks);
        if (swimAmount <= 0.0F) {
            return 0.0F;
        }
        float drop = entity.getEyeHeight(Pose.STANDING) - entity.getEyeHeight(Pose.SWIMMING);
        // getEyeHeight includes the entity's scale, which the entity's frame already has.
        return swimAmount * drop / entity.getScale();
    }

    /**
     * Moves {@code poseStack}, at the model's pose, by the entity's root, center and sneak offset.
     *
     * @param entityFrame the pose as vanilla's {@code setupRotations} starts, in the same render
     * @param bodyYaw     the body yaw (degrees) vanilla turns the entity by
     */
    public static void transformModel(DefinedEntityData<?> data, EntityModel<?> model, PoseStack poseStack, Matrix4fc entityFrame, float bodyYaw) {
        LivingEntity entity = data.getEntity();
        EntityModelDefinition definition = data.getDefinition();

        Matrix4f toModel = new Matrix4f(entityFrame).invertAffine().mul(poseStack.last().pose());
        VanillaDraw.mulPose(poseStack, new Matrix4f(toModel).invertAffine());
        float yaw = (float) Math.toRadians(bodyYaw);
        poseStack.mulPose(new Quaternionf().rotationY(-yaw));

        // 1.12's frame: the entity's feet, facing as its body does, Y up, in blocks.
        poseStack.translate(0.0F, -swimmingDrop(entity), 0.0F);
        float globalScale = entity.isBaby() ? definition.childScale : 1;
        float s = SCALE * globalScale;
        poseStack.translate(data.root.position.getX() * s, data.root.position.getY() * s, data.root.position.getZ() * s);
        poseStack.mulPose(DefinedModelPart.toJoml(data.root.rotation.getSmooth()));
        poseStack.translate(data.center.position.getX() * s, data.center.position.getY() * s, data.center.position.getZ() * s);
        Vec3f pivot = data.getCenterPivot(new Vec3f());
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        poseStack.mulPose(DefinedModelPart.toJoml(data.center.rotation.getSmooth()));
        poseStack.translate(-pivot.x, -pivot.y, -pivot.z);
        transformLocally(entity, definition, poseStack);

        poseStack.mulPose(new Quaternionf().rotationY(yaw));
        VanillaDraw.mulPose(poseStack, toModel);

        // 1.12's ModelBiped lowered a sneaking biped's whole model, in the model's space, before
        // drawing its parts; this version's moves the parts instead, which the bent model doesn't
        // read. The definitions' sneak offsets count on it.
        if (model instanceof HumanoidModel<?> humanoid && humanoid.crouching) {
            poseStack.translate(0.0F, 0.2F, 0.0F);
        }
    }

    /** 1.12's {@code DefinedRenderer.transformLocally}: a sneaking entity is raised by the definition's offset. */
    private static void transformLocally(LivingEntity entity, EntityModelDefinition definition, PoseStack poseStack) {
        EntityModelDefinition.RendererSettings settings = definition.renderer;
        if (entity.isCrouching()) {
            boolean flying = entity instanceof Player player && RemoteFlight.isFlying(player);
            float offset = flying && settings.flyingSneakOffset != null ? settings.flyingSneakOffset : settings.sneakOffset;
            if (offset != 0) {
                poseStack.translate(0.0F, offset * SCALE, 0.0F);
            }
        }
    }
}
