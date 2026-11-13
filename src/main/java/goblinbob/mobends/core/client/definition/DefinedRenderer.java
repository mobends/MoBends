package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.VanillaDraw;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

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
 * <p>
 * What the renderer leaves out of vanilla's turns for the animator (a swimming player's tilt, the
 * golem's rock) or adds in their place (the swimming drop, the sneak offsets) it always does for a
 * bent entity. Where a {@code core:vanilla_pose} node gives the entity vanilla's frame, the root
 * bone carries the difference ({@link #vanillaFrameAsRoot}), so it blends like any bone.
 */
public final class DefinedRenderer {
    private static final float SCALE = 0.0625F;

    /*
     * While vanilla's turns of an entity are worked out ahead of its draw ({@link #framedAs}): the
     * data to take it as bent by, or null for vanilla's own draw. Render thread only.
     */
    private static boolean framing;
    @Nullable
    private static DefinedEntityData<?> framingAs;

    private DefinedRenderer() {
    }

    /**
     * The data a renderer's mixin takes the entity it turns as drawn bent by: {@code bent}, the data
     * it's drawn with, except while its turns are worked out ahead of time ({@link #framedAs}).
     */
    @Nullable
    public static DefinedEntityData<?> drawnBentBy(@Nullable DefinedEntityData<?> bent) {
        return framing ? framingAs : bent;
    }

    /**
     * Runs {@code turns} (a renderer's own turns of the entity, ahead of its draw) with the entity
     * taken as drawn bent by {@code as}, or as vanilla draws it for null ({@link #drawnBentBy}).
     */
    public static void framedAs(@Nullable DefinedEntityData<?> as, Runnable turns) {
        boolean wasFraming = framing;
        DefinedEntityData<?> wasAs = framingAs;
        framing = true;
        framingAs = as;
        try {
            turns.run();
        } finally {
            framing = wasFraming;
            framingAs = wasAs;
        }
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
        VanillaDraw.rotate(poseStack, new Quaternionf().rotationY(-yaw));

        // 1.12's frame: the entity's feet, facing as its body does, Y up, in blocks.
        poseStack.translate(0.0F, -swimmingDrop(entity), 0.0F);
        float s = rootScale(data);
        poseStack.translate(data.root.position.getX() * s, data.root.position.getY() * s, data.root.position.getZ() * s);
        VanillaDraw.rotate(poseStack, DefinedModelPart.toJoml(data.root.rotation.getSmooth()));
        poseStack.translate(data.center.position.getX() * s, data.center.position.getY() * s, data.center.position.getZ() * s);
        Vec3f pivot = data.getCenterPivot(new Vec3f());
        poseStack.translate(pivot.x, pivot.y, pivot.z);
        VanillaDraw.rotate(poseStack, DefinedModelPart.toJoml(data.center.rotation.getSmooth()));
        poseStack.translate(-pivot.x, -pivot.y, -pivot.z);
        poseStack.translate(0.0F, sneakOffset(entity, definition), 0.0F);

        VanillaDraw.rotate(poseStack, new Quaternionf().rotationY(yaw));
        VanillaDraw.mulPose(poseStack, toModel);

        // 26.x builds some models' size into their root's pose (the cave spider's 0.7), which vanilla
        // applies as it draws the root: the bent model's bones hang from it too.
        //? if >=26.1 {
        /*net.minecraft.client.model.geom.PartPose rest = model.root().getInitialPose();
        poseStack.translate(rest.x() / 16.0F, rest.y() / 16.0F, rest.z() / 16.0F);
        poseStack.scale(rest.xScale(), rest.yScale(), rest.zScale());
        *///?}

        poseStack.translate(0.0F, sneakingDrop(entity, model), 0.0F);
    }

    /** What the root's and the center's positions are scaled by, from model units into the entity's frame (a baby's are closer). */
    private static float rootScale(DefinedEntityData<?> data) {
        return SCALE * (data.getEntity().isBaby() ? data.getDefinition().childScale : 1);
    }

    /**
     * 1.12's ModelBiped lowered a sneaking biped's whole model, in the model's space, before drawing
     * its parts; this version's moves the parts instead, which the bent model doesn't read. The
     * definitions' sneak offsets count on it.
     */
    private static float sneakingDrop(LivingEntity entity, EntityModel<?> model) {
        //? if >=26.1 {
        /*return model instanceof HumanoidModel<?> && entity.isCrouching() ? 0.2F : 0.0F;
        *///?} else {
        return model instanceof HumanoidModel<?> humanoid && humanoid.crouching ? 0.2F : 0.0F;
        //?}
    }

    /** 1.12's {@code DefinedRenderer.transformLocally}: how far a sneaking entity is raised, by the definition's offset, in blocks. */
    private static float sneakOffset(LivingEntity entity, EntityModelDefinition definition) {
        EntityModelDefinition.RendererSettings settings = definition.renderer;
        if (!entity.isCrouching()) {
            return 0.0F;
        }
        boolean flying = entity instanceof Player player && RemoteFlight.isFlying(player);
        float offset = flying && settings.flyingSneakOffset != null ? settings.flyingSneakOffset : settings.sneakOffset;
        return offset * SCALE;
    }

    /**
     * The root bone's pose that draws the bent entity, at rest otherwise, in vanilla's frame for it:
     * what vanilla turns the whole entity by that the bent draw leaves out (a swimming player's tilt
     * and shift, the golem's rock), less what it adds in their place (the swimming drop, the sneak
     * offsets), for a {@code core:vanilla_pose} node to give the entity. As the root's pose, it blends
     * into and out of the animator's like any bone's.
     * <p>
     * {@link #transformModel} draws the model at {@code entity * X}, with
     * <pre>
     *     X = Ry(-yaw) * drop * root * sneak * Ry(yaw) * bent * rest * sneakingDrop
     * </pre>
     * where vanilla draws it at {@code entity * vanilla * rest}; the root is what makes the two meet.
     *
     * @param vanilla  the transform from the entity's frame to the model's as vanilla makes it
     *                 ({@code setupRotations}, the flip, the renderer's scale and the 1.501 shift)
     * @param bent     the same, as the renderer makes it for the entity drawn bent
     * @param bodyYaw  the body yaw (degrees) vanilla turns the entity by
     * @param rotation set to the root's rotation
     * @param position set to the root's position, in model units
     */
    public static void vanillaFrameAsRoot(DefinedEntityData<?> data, EntityModel<?> model, Matrix4fc vanilla, Matrix4fc bent, float bodyYaw,
                                          Quaternion rotation, Vec3f position) {
        LivingEntity entity = data.getEntity();
        float yaw = (float) Math.toRadians(bodyYaw);
        Matrix4f rest = new Matrix4f();
        //? if >=26.1 {
        /*net.minecraft.client.model.geom.PartPose pose = model.root().getInitialPose();
        rest.translation(pose.x() / 16.0F, pose.y() / 16.0F, pose.z() / 16.0F).scale(pose.xScale(), pose.yScale(), pose.zScale());
        *///?}
        // root = drop^-1 * Ry(yaw) * vanilla * rest * sneakingDrop^-1 * rest^-1 * bent^-1 * Ry(-yaw) * sneak^-1
        Matrix4f root = new Matrix4f()
                .translation(0.0F, swimmingDrop(entity), 0.0F)
                .rotateY(yaw)
                .mul(vanilla)
                .mul(rest)
                .translate(0.0F, -sneakingDrop(entity, model), 0.0F)
                .mul(new Matrix4f(rest).invert())
                .mul(new Matrix4f(bent).invert())
                .rotateY(-yaw)
                .translate(0.0F, -sneakOffset(entity, data.getDefinition()), 0.0F);
        // A turn and a shift, as the two differ by turns and shifts only (their scales are the renderer's, alike).
        Quaternionf turn = root.getNormalizedRotation(new Quaternionf());
        Vector3f shift = root.getTranslation(new Vector3f()).div(rootScale(data));
        rotation.set(turn.x, turn.y, turn.z, turn.w);
        position.set(shift.x, shift.y, shift.z);
    }
}
