package goblinbob.mobends.core.client.definition;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.client.model.MutatedBox;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A part Mo' Bends draws in place of a vanilla model part: a bone, a split segment of one, or a piece
 * of an overlay. It carries its boxes (read from the vanilla part, cut where the definition asks)
 * and is posed each frame by its bone's {@link ModelPartTransform} in the entity's data, as 1.12's
 * {@code DefinedModelPart} was by {@code syncUp}. Children are drawn inside its transform.
 */
public class DefinedModelPart {
    private static final float SCALE = 0.0625F;

    /** The bone's (or segment's) name in the data; null for a piece the animator doesn't pose. */
    @Nullable
    final String name;
    @Nullable
    final DefinedModelPart parent;
    /** Where it sits relative to its parent, in model units, when the data has no transform for it. */
    final Vec3f position = new Vec3f();
    final List<MutatedBox> boxes = new ArrayList<>();
    final List<DefinedModelPart> children = new ArrayList<>();
    /** A split bone's further segments, in order, which {@link #applyVanillaFrame} goes through; empty otherwise. */
    final List<DefinedModelPart> segments = new ArrayList<>();
    /** The vanilla part's own children that are no bones, drawn as vanilla draws them, inside this part. */
    final List<ModelPart> vanillaChildren = new ArrayList<>();
    /** Where the vanilla children's frame is in this part's (see {@code BoneDefinition#pivot}), in model units. */
    final Vec3f vanillaChildrenShift = new Vec3f();
    /**
     * The vanilla part whose visibility this part follows (the skin customisation toggles and the
     * spectator's head-only drawing set it each frame); null to always show.
     */
    @Nullable
    final ModelPart visibleWith;
    @Nullable
    Quaternion restRotation;
    /** Drawn through its parents' transforms, not by its parent (see {@code BoneDefinition#drawnAlone}). */
    boolean drawnAlone;
    /** For a piece of an overlay (a sleeve), the overlay bone's name; null for a bone's own part. */
    @Nullable
    String overlayOf;

    /*
     * While a part is drawn on its own ({@link DefinedModel#renderAlone}, the first-person hand):
     * whether every bone is drawn at rest, whatever the data says, and which parts' boxes are drawn
     * (their children decide for themselves). Render thread only.
     */
    static boolean allAtRest;
    @Nullable
    static Predicate<DefinedModelPart> drawn;

    DefinedModelPart(@Nullable String name, @Nullable DefinedModelPart parent, @Nullable ModelPart visibleWith) {
        this.name = name;
        this.parent = parent;
        this.visibleWith = visibleWith;
    }

    @Nullable
    private ModelPartTransform transformOf(DefinedEntityData<?> data) {
        return name == null ? null : data.getPart(name);
    }

    /** Whether it's drawn at rest, while drawn on its own (see {@link #allAtRest}). */
    private boolean isAtRest() {
        return allAtRest;
    }

    /**
     * Whether its boxes are drawn. Its children decide for themselves: vanilla shows the head of a
     * spectator, a child of the body here, while the body is hidden.
     */
    private boolean isShowing() {
        return (visibleWith == null || (visibleWith.visible && !visibleWith.skipDraw)) && (drawn == null || drawn.test(this));
    }

    /** Draws the part as a root of the model: through its parents' transforms, if it has any. */
    void renderAsRoot(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, DefinedEntityData<?> data) {
        poseStack.pushPose();
        applyCharacterTransform(poseStack, data, SCALE);
        renderInFrame(poseStack, consumer, packedLight, packedOverlay, color, data);
        poseStack.popPose();
    }

    /**
     * Draws the part in the frame vanilla's part has ({@code poseStack} is there): its own frame
     * is that, moved back by where vanilla's part turns from where the bone does (the reverse of
     * the end of {@link #applyVanillaFrame}).
     */
    void renderInVanillaFrame(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, DefinedEntityData<?> data) {
        poseStack.pushPose();
        poseStack.translate(-vanillaChildrenShift.x * SCALE, -vanillaChildrenShift.y * SCALE, -vanillaChildrenShift.z * SCALE);
        renderInFrame(poseStack, consumer, packedLight, packedOverlay, color, data);
        poseStack.popPose();
    }

    /** Draws the part inside its parent's transform. */
    private void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, DefinedEntityData<?> data) {
        poseStack.pushPose();
        applyLocalTransform(poseStack, data, SCALE);
        renderInFrame(poseStack, consumer, packedLight, packedOverlay, color, data);
        poseStack.popPose();
    }

    private void renderInFrame(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color, DefinedEntityData<?> data) {
        if (isShowing()) {
            PoseStack.Pose pose = poseStack.last();
            for (MutatedBox box : boxes) {
                box.render(pose, consumer, packedLight, packedOverlay, color);
            }
            if (!vanillaChildren.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(vanillaChildrenShift.x * SCALE, vanillaChildrenShift.y * SCALE, vanillaChildrenShift.z * SCALE);
                for (ModelPart child : vanillaChildren) {
                    child.render(poseStack, consumer, packedLight, packedOverlay, color);
                }
                poseStack.popPose();
            }
        }
        for (DefinedModelPart child : children) {
            child.render(poseStack, consumer, packedLight, packedOverlay, color, data);
        }
    }

    /**
     * 1.12's {@code DefinedModelPart.postRender}: moves {@code poseStack}, at the model's pose, into
     * the frame vanilla's part has, posed as the bone is, for what vanilla attaches to the part (a
     * held item, a hat, the cape). That is the part's own frame, through its parents', then through
     * each further segment, each turning about its joint and moving back by where the joint is, so
     * whatever hangs off the end follows the last segment (a held item, the forearm); then back to
     * where vanilla's part turns, if the bone turns elsewhere ({@code BoneDefinition#pivot}).
     *
     * @param nudgeX how far vanilla moves its part before turning it, in model units, in the
     *               parent's frame (the slim player model's hand); usually 0
     */
    void applyVanillaFrame(PoseStack poseStack, DefinedEntityData<?> data, float nudgeX, float nudgeY, float nudgeZ) {
        applyParentTransforms(poseStack, data, SCALE);
        if (nudgeX != 0 || nudgeY != 0 || nudgeZ != 0) {
            poseStack.translate(nudgeX * SCALE, nudgeY * SCALE, nudgeZ * SCALE);
        }
        applyLocalTransform(poseStack, data, SCALE);
        for (DefinedModelPart segment : segments) {
            segment.applyLocalTransform(poseStack, data, SCALE);
            // Back by as much as applyLocalTransform moved it, so the two cancel at rest.
            ModelPartTransform transform = segment.transformOf(data);
            Vec3f at = transform != null ? transform.position : segment.position;
            float scale = SCALE * (transform != null ? transform.offsetScale : 1);
            poseStack.translate(-at.x * scale, -at.y * scale, -at.z * scale);
        }
        if (vanillaChildrenShift.x != 0 || vanillaChildrenShift.y != 0 || vanillaChildrenShift.z != 0) {
            poseStack.translate(vanillaChildrenShift.x * SCALE, vanillaChildrenShift.y * SCALE, vanillaChildrenShift.z * SCALE);
        }
    }

    /** Applies the part's own transform alone (in its parent's frame), as it is drawn. */
    void applyLocalTransform(PoseStack poseStack, DefinedEntityData<?> data) {
        applyLocalTransform(poseStack, data, SCALE);
    }

    /** 1.12's {@code IModelPart.applyCharacterTransform}: the global offset, the parents' transforms, then its own. */
    private void applyCharacterTransform(PoseStack poseStack, DefinedEntityData<?> data, float scale) {
        applyParentTransforms(poseStack, data, scale);
        applyLocalTransform(poseStack, data, scale);
    }

    /** The first half of {@link #applyCharacterTransform}: the global offset and the parents' transforms. */
    private void applyParentTransforms(PoseStack poseStack, DefinedEntityData<?> data, float scale) {
        ModelPartTransform transform = transformOf(data);
        if (transform != null) {
            Vec3f globalOffset = transform.globalOffset;
            if (globalOffset.x != 0 || globalOffset.y != 0 || globalOffset.z != 0) {
                poseStack.translate(globalOffset.x * scale, globalOffset.y * scale, globalOffset.z * scale);
            }
        }
        if (parent != null) {
            parent.applyCharacterTransform(poseStack, data, scale * (transform != null ? transform.offsetScale : 1));
        }
    }

    /**
     * 1.12's {@code ModelPart.applyLocalTransform}: moves to where it sits (and by its animated
     * offset), turns back by its parents' rotation if it doesn't turn with them, turns by its rest
     * rotation and its own, then scales.
     */
    private void applyLocalTransform(PoseStack poseStack, DefinedEntityData<?> data, float scale) {
        ModelPartTransform transform = transformOf(data);
        Vec3f at = transform != null ? transform.position : position;
        float offsetScale = transform != null ? transform.offsetScale : 1;
        if (at.x != 0 || at.y != 0 || at.z != 0) {
            poseStack.translate(at.x * scale * offsetScale, at.y * scale * offsetScale, at.z * scale * offsetScale);
        }
        if (transform == null) {
            if (restRotation != null) {
                poseStack.mulPose(toJoml(restRotation));
            }
            return;
        }
        if (isAtRest()) {
            // Where it sits, turned by its rest rotation alone.
            if (restRotation != null) {
                poseStack.mulPose(toJoml(restRotation));
            }
            return;
        }
        Vec3f offset = transform.offset;
        if (offset.x != 0 || offset.y != 0 || offset.z != 0) {
            poseStack.translate(offset.x * scale * offsetScale, offset.y * scale * offsetScale, offset.z * scale * offsetScale);
        }
        applyParentRotationUndone(poseStack, data);
        if (restRotation != null) {
            poseStack.mulPose(toJoml(restRotation));
        }
        poseStack.mulPose(toJoml(transform.rotation.getSmooth()));
        Vec3f partScale = transform.scale;
        if (partScale.x != 1 || partScale.y != 1 || partScale.z != 1) {
            poseStack.scale(partScale.x, partScale.y, partScale.z);
        }
    }

    private boolean inheritsRotation(DefinedEntityData<?> data) {
        ModelPartTransform transform = transformOf(data);
        return transform == null || transform.inheritRotation;
    }

    /** The rotation the part adds to its frame: its own, after its rest rotation. */
    private void getLocalRotation(DefinedEntityData<?> data, Quaternion dest) {
        ModelPartTransform transform = transformOf(data);
        if (transform == null || isAtRest()) {
            if (restRotation != null) dest.set(restRotation);
            else dest.setIdentity();
            return;
        }
        if (restRotation != null) Quaternion.mul(restRotation, transform.rotation.getSmooth(), dest);
        else dest.set(transform.rotation.getSmooth());
    }

    /** The rotation of the part's frame relative to the model, up to the first part that doesn't turn with its parent. */
    private void getModelRotation(DefinedEntityData<?> data, Quaternion dest) {
        getLocalRotation(data, dest);
        Quaternion parentRotation = new Quaternion();
        for (DefinedModelPart part = this; part.inheritsRotation(data) && part.parent != null; part = part.parent) {
            part.parent.getLocalRotation(data, parentRotation);
            Quaternion.mul(parentRotation, dest, dest);
        }
    }

    /**
     * For a part that doesn't turn with its parent (the head): turns back by its parents' rotation,
     * so its own is relative to the model. Its position still follows the parent.
     */
    private void applyParentRotationUndone(PoseStack poseStack, DefinedEntityData<?> data) {
        if (inheritsRotation(data) || parent == null) {
            return;
        }
        Quaternion parentRotation = new Quaternion();
        parent.getModelRotation(data, parentRotation);
        parentRotation.conjugate();
        poseStack.mulPose(toJoml(parentRotation));
    }

    /**
     * The same rotation as JOML's. 1.12 turned GL's matrix by the quaternion's rotation matrix
     * ({@code GlHelper.rotate}, the usual one for a unit quaternion, laid out column by column),
     * which is the matrix JOML makes of the same x, y, z, w, so the components carry over as they are.
     */
    public static Quaternionf toJoml(Quaternion q) {
        return new Quaternionf(q.x, q.y, q.z, q.w);
    }
}
