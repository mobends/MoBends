package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The pose the vanilla model has this frame, as a {@code core:vanilla_pose} node reads it
 * ({@link IVanillaPose}), for one entity: the renderer opens it around the entity's animation
 * ({@link #open}), and the first read poses the vanilla model (vanilla's {@code setupAnim}, with
 * whatever other mods add to it) and works out every bone from it.
 * <p>
 * Each bone that takes over a vanilla part is given the rotation and offset that draw it where
 * vanilla draws that part, through the bones above it as they're given here. The bone keeps where
 * it sits from the part at rest, as the definition places it: its pivot may be elsewhere (a body
 * that bends at the hips, a wolf's leg turning a pixel off vanilla's) and its boxes turned another
 * way (the wolf's body lies along Z, vanilla's is turned up), so its frame is the part's posed
 * frame, times the part's rest frame undone, times its own rest frame. Vanilla's parts hang from
 * the model's root, the bones from each other (a head that doesn't turn with the body), so a bone's
 * own rotation is what's left once its parents' is taken out. A bone vanilla draws as a piece of an
 * ancestor's part, straight with it (a forearm, the wolf's shins and ears), is at rest. Any other
 * bone that stands for no vanilla part has nothing to read; the bones under it are worked out as
 * if it were at rest.
 * <p>
 * Not carried: a vanilla part's scale, the root part's own pose (the model's space is its frame,
 * as for the bent model), and a baby's offset scale.
 */
public final class VanillaPoseReader implements IVanillaPose {
    private static boolean warned;

    private final DefinedEntityData<?> data;
    /** What's open: the bent parts, and what poses the vanilla model; null while closed. */
    @Nullable
    private DefinedModel parts;
    @Nullable
    private Runnable poseVanilla;
    private boolean solved;

    /** Per part of the bent model, what it's worked out to; by name, the bones a read can ask for. */
    private final Map<DefinedModelPart, Solved> byPart = new IdentityHashMap<>();
    private final Map<String, Solved> byName = new HashMap<>();
    @Nullable
    private DefinedModel solvedFor;

    // Scratch.
    private final Matrix4f matrix = new Matrix4f();
    private final Quaternionf turn = new Quaternionf();
    private final Quaternionf inverse = new Quaternionf();
    private final Vector3f vector = new Vector3f();
    private final Quaternionf partTurn = new Quaternionf();
    private final Quaternionf restTurn = new Quaternionf();
    private final Vector3f restOrigin = new Vector3f();

    private static final class Solved {
        /** Whether a read gets it: a bone posed from its vanilla part, or one drawn with an ancestor's (at rest). */
        boolean posed;
        final Quaternion rotation = new Quaternion();
        final Vec3f offset = new Vec3f();
        /** Its frame in the model's space, as drawn with what it's given: where its children hang from. */
        final Quaternionf frameRotation = new Quaternionf();
        final Vector3f frameOrigin = new Vector3f();
        /** Its frame in the model's space at rest, as the definition places it. */
        final Quaternionf restRotation = new Quaternionf();
        final Vector3f restOrigin = new Vector3f();
    }

    public VanillaPoseReader(DefinedEntityData<?> data) {
        this.data = data;
    }

    /**
     * Opens the reader for this frame's animation of the entity, drawn with {@code parts}.
     * {@code poseVanilla} poses the vanilla model for the entity, as it is about to be drawn; it
     * runs once, at the first read, if any.
     */
    public void open(DefinedModel parts, Runnable poseVanilla) {
        this.parts = parts;
        this.poseVanilla = poseVanilla;
        this.solved = false;
    }

    /** Closes it: nothing is read until it's opened again. */
    public void close() {
        this.parts = null;
        this.poseVanilla = null;
    }

    @Override
    public boolean read(String bone, Quaternion rotation, Vec3f offset) {
        DefinedModel model = parts;
        if (model == null) {
            return false;
        }
        if (!solved) {
            solved = true;
            try {
                poseVanilla.run();
                solve(model);
            } catch (RuntimeException e) {
                // Another mod's setupAnim failing: the node poses nothing, rather than the entity not drawing.
                if (!warned) {
                    warned = true;
                    MoBends.LOGGER.warn("Couldn't read the vanilla pose of {}; core:vanilla_pose poses nothing for it.", data.getEntity(), e);
                }
                byName.clear();
                byPart.clear();
                solvedFor = null;
            }
        }
        Solved solved = byName.get(bone);
        if (solved == null || !solved.posed) {
            return false;
        }
        rotation.set(solved.rotation);
        offset.set(solved.offset);
        return true;
    }

    private void solve(DefinedModel model) {
        if (solvedFor != model) {
            byPart.clear();
            byName.clear();
            solvedFor = model;
        }
        for (DefinedModelPart part : model.partsInOrder()) {
            Solved solved = byPart.get(part);
            if (solved == null) {
                solved = new Solved();
                byPart.put(part, solved);
                if (part.name != null) {
                    byName.put(part.name, solved);
                }
            }
            solve(part, solved);
        }
    }

    /** Works out {@code part}, whose parent is worked out already (it comes first in the model's order). */
    private void solve(DefinedModelPart part, Solved solved) {
        Solved parent = part.parent == null ? null : byPart.get(part.parent);
        ModelPartTransform transform = part.name == null ? null : data.getPart(part.name);
        Vec3f position = transform != null ? transform.position : part.position;
        boolean inherits = transform == null || transform.inheritRotation;
        Quaternionf rest = rest(part);

        // Its frame at rest, as the definition places it: where it sits in its parent's.
        //   frame = parent * translate(position) * [parent's rotation undone] * rest
        if (parent != null) {
            solved.restRotation.set(parent.restRotation);
            solved.restOrigin.set(parent.restOrigin);
        } else {
            solved.restRotation.identity();
            solved.restOrigin.zero();
        }
        vector.set(part.position.x, part.position.y, part.position.z);
        solved.restOrigin.add(solved.restRotation.transform(vector));
        if (!inherits) {
            solved.restRotation.identity();
        }
        if (rest != null) {
            solved.restRotation.mul(rest);
        }

        // The parent's frame as given here (the model's space for a root).
        Quaternionf parentRotation = solved.frameRotation;
        Vector3f parentOrigin = solved.frameOrigin;
        if (parent != null) {
            parentRotation.set(parent.frameRotation);
            parentOrigin.set(parent.frameOrigin);
        } else {
            parentRotation.identity();
            parentOrigin.zero();
        }
        //   frame = parent * translate(position + offset) * [parent's rotation undone] * rest * rotation
        ModelPart[] chain = part.vanillaChain;
        if (chain == null) {
            // At rest: no rotation of its own, no offset.
            solved.posed = part.drawnWithVanillaPart;
            solved.rotation.setIdentity();
            solved.offset.set(0, 0, 0);
            vector.set(position.x, position.y, position.z);
            parentOrigin.add(parentRotation.transform(vector));
            if (!inherits) {
                parentRotation.identity();
            }
            if (rest != null) {
                parentRotation.mul(rest);
            }
            // parentRotation and parentOrigin are the solved frame's own fields: they now hold its frame.
            return;
        }

        // Vanilla's frame for the part, posed (V) and as the model was made (V0): its parents' poses,
        // then its own, as vanilla draws it.
        matrix.identity();
        Quaternionf vanillaRotation = turn.identity();
        Quaternionf vanillaRestRotation = restTurn.identity();
        Vector3f vanillaRestOrigin = restOrigin.zero();
        for (int i = 0; i < chain.length; i++) {
            ModelPart vanilla = chain[i];
            matrix.translate(vanilla.x, vanilla.y, vanilla.z);
            partTurn.rotationZYX(vanilla.zRot, vanilla.yRot, vanilla.xRot);
            matrix.rotate(partTurn);
            vanillaRotation.mul(partTurn);
            if (vanilla.xScale != 1 || vanilla.yScale != 1 || vanilla.zScale != 1) {
                matrix.scale(vanilla.xScale, vanilla.yScale, vanilla.zScale);
            }
            float[] at = part.vanillaChainRest[i];
            vanillaRestOrigin.add(vanillaRestRotation.transform(vector.set(at[0], at[1], at[2])));
            vanillaRestRotation.mul(partTurn.rotationZYX(at[5], at[4], at[3]));
        }
        // The bone keeps where it sits from vanilla's part at rest (its boxes are where the part's
        // are, however the definition placed its pivot and turned its boxes): its frame is
        // V * V0^-1 * its rest frame.
        vanillaRestRotation.conjugate();
        Vector3f origin = vanillaRestRotation.transform(vector.set(solved.restOrigin).sub(vanillaRestOrigin), new Vector3f());
        matrix.transformPosition(origin);
        Quaternionf frame = new Quaternionf(vanillaRotation).mul(vanillaRestRotation).mul(solved.restRotation);

        // Its rotation: that, less its parent's (unless it doesn't turn with it), less its rest rotation.
        Quaternionf local = new Quaternionf(frame);
        if (inherits) {
            parentRotation.conjugate(inverse).mul(local, local);
        }
        if (rest != null) {
            rest.conjugate().mul(local, local);
        }
        solved.rotation.set(local.x, local.y, local.z, local.w);

        // Its offset: from where it sits in its parent's frame to where that frame puts it.
        vector.set(origin).sub(parentOrigin);
        parentRotation.conjugate(inverse).transform(vector);
        solved.offset.set(vector.x - position.x, vector.y - position.y, vector.z - position.z);
        solved.posed = true;

        solved.frameRotation.set(frame);
        solved.frameOrigin.set(origin);
    }

    @Nullable
    private static Quaternionf rest(DefinedModelPart part) {
        Quaternion rest = part.restRotation;
        return rest == null ? null : new Quaternionf(rest.x, rest.y, rest.z, rest.w);
    }
}
