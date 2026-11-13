package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.OrientationComponent;
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
 * its pivot sits from the part at rest, as the definition places it (a body that bends at the
 * hips, a wolf's leg turning a pixel off vanilla's), and the quarter turns its boxes are modelled
 * by against the part's (the wolf's body lies along Z, vanilla's stands up and is turned down);
 * any other angle between them at rest is a rest pose the definition doesn't share (vanilla's
 * wolf tail tilts back, the definition's hangs straight), and vanilla's angle shows. Vanilla's parts hang from
 * the model's root, the bones from each other (a head that doesn't turn with the body), so a bone's
 * own rotation is what's left once its parents' is taken out. A bone vanilla draws as a piece of an
 * ancestor's part, straight with it (a forearm, the wolf's shins and ears), is at rest, and so is a
 * {@code core:orientation} component (how a held item turns in the hand). Any other
 * bone that stands for no vanilla part has nothing to read; the bones under it are worked out as
 * if it were at rest.
 * <p>
 * A bone is scaled as its vanilla part is against the part's rest scale (Player Animation Library
 * scales parts), less what the bones above it scale it by already; its scale is taken along the
 * axes its boxes are modelled by against the part's, and its parent's along the nearest axes to
 * its own, so a scale is carried exactly where the two frames are a quarter turn apart.
 * <p>
 * Not carried: the root part's own pose (the model's space is its frame, as for the bent model),
 * and a baby's offset scale.
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
    private final Vector3f parentScale = new Vector3f();
    private final Vector3f vanillaScale = new Vector3f();

    private static final class Solved {
        /** Whether a read gets it: a bone posed from its vanilla part, or one drawn with an ancestor's (at rest). */
        boolean posed;
        final Quaternion rotation = new Quaternion();
        final Vec3f offset = new Vec3f();
        final Vec3f scale = new Vec3f(1, 1, 1);
        /** Its frame in the model's space, as drawn with what it's given: where its children hang from. */
        final Quaternionf frameRotation = new Quaternionf();
        final Vector3f frameOrigin = new Vector3f();
        /** Its frame's scale, along its own axes, as drawn with what it's given (what its children are drawn with). */
        final Vector3f frameScale = new Vector3f(1, 1, 1);
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
    public boolean read(String bone, Quaternion rotation, Vec3f offset, Vec3f scale) {
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
        if (solved == null && data.getComponent(bone, OrientationComponent.class) != null) {
            // How a held item turns in the hand (core:orientation): vanilla holds it unturned.
            rotation.setIdentity();
            offset.set(0, 0, 0);
            scale.set(1, 1, 1);
            return true;
        }
        if (solved == null || !solved.posed) {
            return false;
        }
        rotation.set(solved.rotation);
        offset.set(solved.offset);
        scale.set(solved.scale);
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
            parentScale.set(parent.frameScale);
        } else {
            parentRotation.identity();
            parentOrigin.zero();
            parentScale.set(1, 1, 1);
        }
        //   frame = parent * translate(position + offset) * [parent's rotation undone] * rest * rotation * scale
        ModelPart[] chain = part.vanillaChain;
        if (chain == null) {
            // At rest: no rotation of its own, no offset, no scale; it sits in its parent's scaled frame.
            solved.posed = part.drawnWithVanillaPart;
            solved.rotation.setIdentity();
            solved.offset.set(0, 0, 0);
            solved.scale.set(1, 1, 1);
            vector.set(position.x, position.y, position.z).mul(parentScale);
            parentOrigin.add(parentRotation.transform(vector));
            Quaternionf local = inherits ? new Quaternionf() : new Quaternionf(parentRotation).conjugate();
            if (!inherits) {
                parentRotation.identity();
            }
            if (rest != null) {
                parentRotation.mul(rest);
                local.mul(rest);
            }
            alongAxes(local, parentScale, solved.frameScale);
            // parentRotation and parentOrigin are the solved frame's own fields: they now hold its frame.
            return;
        }

        // Vanilla's frame for the part, posed (V) and as the model was made (V0): its parents' poses,
        // then its own, as vanilla draws it.
        matrix.identity();
        Quaternionf vanillaRotation = turn.identity();
        Quaternionf vanillaRestRotation = restTurn.identity();
        Vector3f vanillaRestOrigin = restOrigin.zero();
        // Its parts' scales against their rest scales, along the last part's axes (as if the parts were turned by quarter turns).
        vanillaScale.set(1, 1, 1);
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
            alongAxes(partTurn, vanillaScale, vanillaScale);
            vanillaScale.mul(ratio(vanilla.xScale, at[6]), ratio(vanilla.yScale, at[7]), ratio(vanilla.zScale, at[8]));
            vanillaRestOrigin.add(vanillaRestRotation.transform(vector.set(at[0], at[1], at[2])));
            vanillaRestRotation.mul(partTurn.rotationZYX(at[5], at[4], at[3]));
        }
        // The bone keeps where its pivot sits from vanilla's part at rest (V * V0^-1 * its rest
        // origin), and the quarter turns its boxes are modelled by against the part's: the rest of
        // what turns it from the part at rest is a rest pose the definition doesn't share (vanilla
        // tilts the wolf's tail back, the definition hangs it straight and the animator lifts it),
        // so vanilla's angle shows.
        vanillaRestRotation.conjugate();
        Vector3f origin = vanillaRestRotation.transform(vector.set(solved.restOrigin).sub(vanillaRestOrigin), new Vector3f());
        matrix.transformPosition(origin);
        Quaternionf boxTurn = nearestQuarterTurns(vanillaRestRotation.mul(solved.restRotation));
        Quaternionf frame = new Quaternionf(vanillaRotation).mul(boxTurn);
        // Its scale along its own axes (where its boxes' axes lie on the part's), less its parent's.
        Vector3f frameScale = alongAxes(boxTurn, vanillaScale, new Vector3f());
        Vector3f fromParent = alongAxes(new Quaternionf(parentRotation).conjugate().mul(frame), parentScale, new Vector3f());
        solved.scale.set(frameScale.x / fromParent.x, frameScale.y / fromParent.y, frameScale.z / fromParent.z);

        // Its rotation: that, less its parent's (unless it doesn't turn with it), less its rest rotation.
        Quaternionf local = new Quaternionf(frame);
        if (inherits) {
            parentRotation.conjugate(inverse).mul(local, local);
        }
        if (rest != null) {
            rest.conjugate().mul(local, local);
        }
        solved.rotation.set(local.x, local.y, local.z, local.w);

        // Its offset: from where it sits in its parent's (scaled) frame to where that frame puts it.
        vector.set(origin).sub(parentOrigin);
        parentRotation.conjugate(inverse).transform(vector).div(parentScale);
        solved.offset.set(vector.x - position.x, vector.y - position.y, vector.z - position.z);
        solved.posed = true;

        solved.frameRotation.set(frame);
        solved.frameOrigin.set(origin);
        solved.frameScale.set(frameScale);
    }

    /**
     * {@code scale}, along the axes of a frame turned by {@code turn} from the one it's given in:
     * each of the turned frame's axes takes the scale of the axis it lies nearest. {@code dest} may
     * be {@code scale}.
     */
    private Vector3f alongAxes(Quaternionf turn, Vector3f scale, Vector3f dest) {
        float x = scale.get(nearestAxis(turn, 1, 0, 0));
        float y = scale.get(nearestAxis(turn, 0, 1, 0));
        float z = scale.get(nearestAxis(turn, 0, 0, 1));
        return dest.set(x, y, z);
    }

    /** The axis (0, 1, 2) that {@code turn} brings the given one nearest. */
    private int nearestAxis(Quaternionf turn, float x, float y, float z) {
        turn.transform(vector.set(x, y, z));
        float ax = Math.abs(vector.x), ay = Math.abs(vector.y), az = Math.abs(vector.z);
        return ax >= ay && ax >= az ? 0 : ay >= az ? 1 : 2;
    }

    /** A part's scale against its rest scale, 1 where the rest is no scale at all. */
    private static float ratio(float scale, float rest) {
        return rest == 0 ? 1 : scale / rest;
    }

    /** The 24 turns that take the axes onto the axes. */
    private static final Quaternionf[] QUARTER_TURNS = quarterTurns();

    private static Quaternionf[] quarterTurns() {
        java.util.List<Quaternionf> turns = new java.util.ArrayList<>();
        float quarter = (float) (Math.PI / 2);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    Quaternionf turn = new Quaternionf().rotationXYZ(x * quarter, y * quarter, z * quarter);
                    boolean known = false;
                    for (Quaternionf other : turns) {
                        known |= Math.abs(other.dot(turn)) > 0.999F;
                    }
                    if (!known) {
                        turns.add(turn);
                    }
                }
            }
        }
        return turns.toArray(new Quaternionf[0]);
    }

    /** The turn of {@link #QUARTER_TURNS} nearest {@code turn} (which is overwritten with it). */
    private static Quaternionf nearestQuarterTurns(Quaternionf turn) {
        Quaternionf nearest = QUARTER_TURNS[0];
        float best = -1;
        for (Quaternionf candidate : QUARTER_TURNS) {
            float dot = Math.abs(candidate.dot(turn));
            if (dot > best) {
                best = dot;
                nearest = candidate;
            }
        }
        return turn.set(nearest);
    }

    @Nullable
    private static Quaternionf rest(DefinedModelPart part) {
        Quaternion rest = part.restRotation;
        return rest == null ? null : new Quaternionf(rest.x, rest.y, rest.z, rest.w);
    }
}
