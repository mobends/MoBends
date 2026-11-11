package goblinbob.mobends.core.kumo.state.template.pose;

import com.google.gson.JsonPrimitive;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.List;
import java.util.Map;

/**
 * {@code {"core:two_bone_ik": {...}}} and {@code {"core:three_bone_ik": {...}}}: a chain of bones
 * (an arm, a leg) put onto a point in the world, each joint pointing a way in the world.
 *
 * <p>The chain is its {@link #segments}, each a bone and where it turns at rest, and its
 * {@link #end}, the point that reaches. A joint is a hinge about the X axis of the segment before
 * it, as a split's joints are (they bend on the side of their hinge), and points
 * {@link Segment#bendDirection}: the elbow out to that side. The driver puts the chain where the
 * renderer draws it, from the bones as the items before it have posed them (the whole-model
 * offsets and rotations, {@link #parents}), so the target is met wherever the entity is drawn: one
 * leaning, or held at a point by {@code core:anchor}. It assumes the bones have no
 * {@code restRotation}, and that the renderer draws the model as vanilla does, unscaled but for
 * {@link #modelScale}.
 *
 * <p>Out of reach, the chain points at the target as straight as it gets (or folds as far as it
 * goes, for a target too near); {@code stretch} says how far its end is from the target.
 */
public class ChainIkTemplate extends DriverItemTemplate
{

    /** A bone of the chain. */
    public static class Segment
    {
        public String bone;
        /** Where it turns at rest, in model units (+Y down, -Z forward): the first's in the model (or its last parent), the others' from the one before. */
        public float[] pivot;
        /** The sign the joint at its pivot bends by about X: 1 brings the end round under it to +Z (a knee), -1 to -Z (an elbow). Not the first's. */
        public float bend = 1F;
        /**
         * Where the joint at its pivot points, in the world (a number expression per axis, any
         * length): the side it sticks out to. Left out (or along the reach), the way it points as
         * posed so far. Not the first's.
         */
        public PointTemplate bendDirection;
    }

    /** A bone the chain hangs from (a body the arms are children of), outermost first. */
    public static class Parent
    {
        public String bone;
        /** Where it turns at rest, in model units: the first's in the model, the others' in the one before. */
        public float[] pivot;
    }

    public List<Segment> segments;
    /** The point that reaches the target, in model units from the last segment's pivot, at rest. */
    public float[] end;
    /** The target in the world, in blocks: double expressions. */
    public PointTemplate target;
    /** The bones the chain hangs from, if any. */
    public List<Parent> parents;

    /** How much of the way to the IK the bones go, 0..1. At 1 they snap to it, so the end doesn't slide; at 0 the driver writes nothing. */
    public ExpressionTemplate weight;

    /** The states its outputs go to: {@code stretch}, how far the end is from the target, in blocks (0 where it reaches). */
    public Map<String, String> out;

    /** The body's yaw (a number, degrees), as the renderer has it. */
    public ExpressionTemplate yaw = new ExpressionTemplate(new JsonPrimitive("entityBodyYaw"));
    /** The entity's position (doubles, blocks), as the renderer has it. */
    public ExpressionTemplate x = new ExpressionTemplate(new JsonPrimitive("entityWorldX"));
    public ExpressionTemplate y = new ExpressionTemplate(new JsonPrimitive("entityWorldY"));
    public ExpressionTemplate z = new ExpressionTemplate(new JsonPrimitive("entityWorldZ"));
    /**
     * Where the renderer turns {@link #centerBone} about, in blocks from the entity's feet (+Y up, +Z
     * ahead of the body): a number expression per axis, the entity's own ({@code centerPivotX},
     * {@code centerPivotY}, {@code centerPivotZ}) where left out.
     */
    public PointTemplate centerPivot;

    /** The bone the whole model hangs from (moved, then turned, in the body's frame). */
    public String rootBone = "root";
    /** The child of {@link #rootBone} the rest of the model hangs from (moved, then turned about {@link #centerPivot}). */
    public String centerBone = "center";

    /** Model units per block. */
    public float unitsPerBlock = 16F;
    /** The scale the renderer draws the model at (the player's draws it at 0.9375). */
    public float modelScale = 1F;

}
