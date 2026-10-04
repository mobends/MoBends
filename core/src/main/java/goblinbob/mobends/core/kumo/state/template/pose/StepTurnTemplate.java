package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.List;
import java.util.Map;

/**
 * {@code {"core:step_turn": {...}}}: the body turns by stepping. The driver keeps the yaw the
 * body is shown at, cancels the entity's own body yaw with {@link #rotationBone}, plants the feet
 * in the world and turns the body only as far as its feet have gone: when the entity's body yaw
 * gets ahead of the shown one, the leg on the side of the turn steps first, the body follows, then
 * the other legs catch up. A foot pushed away from under the body steps back too. The legs are put
 * on their feet by a two-segment IK.
 *
 * <p>It walks the same way: a foot left behind by the moving body steps to where it will be under
 * the body half a stride ahead, judged by the entity's velocity, and the steps quicken as it goes
 * faster so a stride stays within {@link #strideLength}.
 *
 * <p>Publishes node variables for the items after it: {@code turnLag} (degrees the entity's body
 * yaw is ahead of the shown one, what a head that looks by {@code headYaw} has to add),
 * {@code turnSpeed} (degrees per tick the shown body turns at), {@code stepLift} (how high the
 * stepping foot is, 0..1, negative for a foot on the -X side) and {@code stepImpact} (0..1, peaks
 * a moment after a foot lands) and {@code stride} (how far the feet on the +X side are ahead of
 * those on the -X side, halved, in model units: what an arm swing follows). All are scaled by the
 * weight.
 */
public class StepTurnTemplate extends DriverItemTemplate
{

    /**
     * How much of the driver applies, 0..1 (e.g. a ramp that goes up while the entity stands).
     * At 0 the body shows the entity's own yaw, the legs what lies beneath, and the feet are placed
     * anew under the body, ready for when it goes up again.
     */
    public ExpressionTemplate weight;

    /** The variable holding the yaw the body is to face, in degrees. */
    public String yawVariable = "bodyYaw";
    /** The variables holding the entity's position in the world, in blocks. */
    public String xVariable = "worldX";
    public String zVariable = "worldZ";

    /**
     * The states its outputs go to, by output: {@code turnLag}, {@code turnSpeed}, {@code stepLift},
     * {@code stepImpact}, {@code stride} (see the class comment), e.g.
     * {@code {"turnSpeed": "node.turnSpeed"}}. An output left out isn't written.
     */
    public Map<String, String> out;

    /** Turns the whole model about its origin; the driver's counter-rotation is put before what is beneath. */
    public String rotationBone = "renderRotation";
    /** Moves the whole model in the shown body's frame: the hips' dip and weight shift. */
    public String offsetBone = "localOffset";

    /** Model units per block. */
    public float unitsPerBlock = 16F;

    public List<Leg> legs;

    /** Degrees the entity's body yaw may get ahead of a foot before that foot steps. */
    public float turnThreshold = 15F;
    /**
     * For {@link #finishWindow} ticks after a foot lands, a foot steps once it's this many degrees
     * off, so a turn ends with the feet squared up instead of just under {@link #turnThreshold}.
     */
    public float finishThreshold = 4F;
    public float finishWindow = 20F;
    /** The most a foot turns in one step, in degrees. */
    public float maxStepAngle = 45F;
    /** Model units a foot may be off its place under the body before it steps. */
    public float driftThreshold = 4F;
    /** For {@link #finishWindow} ticks after a foot lands, a foot this far off its place steps too, so a walk ends with the feet together. */
    public float finishDrift = 1.5F;
    /** A foot further than this from its place (model units) makes every foot be placed anew at once (a knockback, a teleport). */
    public float resetDistance = 16F;

    /** Ticks a step takes. */
    public float stepDuration = 8F;
    /** How high a foot lifts, in model units. */
    public float stepHeight = 3F;
    /** Ticks after a foot lands before the next one may lift. */
    public float stepPause = 2F;
    /**
     * Walking: the farthest a foot travels in one step, in model units. The faster the entity
     * goes, the shorter the steps (down to {@link #minStepDuration}) and the pauses after them take
     * to keep within it.
     */
    public float strideLength = 14F;
    public float minStepDuration = 4F;
    /** Ticks over which the entity's velocity is smoothed before the steps lead by it. */
    public float velocitySmoothing = 3F;
    /** The next foot lifts only once the body has turned to within this many degrees of its feet. */
    public float settleAngle = 3F;

    /** The body turns towards its feet like a mass on a spring: stiffness per tick², damping per tick. */
    public float bodyStiffness = 0.12F;
    public float bodyDamping = 0.55F;
    /**
     * Moving at this many blocks a tick or faster (easing in from 80% of it), the mob runs: the body
     * turns the way it goes instead of waiting for its feet, on a stiffer spring
     * ({@link #runBodyStiffness}, {@link #runBodyDamping}), the feet catching up with the next
     * steps, which go up to {@link #runStrideLength} with no pause between them and lift
     * {@link #runStepHeight}. 0 never runs.
     */
    public float runSpeed = 0F;
    public float runBodyStiffness = 0.5F;
    public float runBodyDamping = 1.3F;
    public float runStrideLength = 20F;
    public float runStepHeight = 3F;
    /**
     * Ticks over which the way the mob goes is smoothed before a running body turns to it, so a
     * path zigzagging from block to block doesn't swing it about.
     */
    public float runFacingSmoothing = 4F;

    /** Model units the hips are lowered by while standing, so the legs have some bend to work with. */
    public float crouch = 0.5F;
    /**
     * Model units the hips are lowered by further while walking (fully once the steps quicken), so
     * the legs reach farther ahead and behind.
     */
    public float walkCrouch = 1F;
    /** How far the hips dip when a foot lands, in model units. */
    public float impactDepth = 1.5F;
    /** Ticks after landing at which the dip is deepest. */
    public float impactTime = 2.5F;
    /** Model units the hips shift over the planted feet while a foot is up. */
    public float weightShift = 1F;
    /**
     * Degrees the whole model rolls over the planted feet while a foot is up, about the entity's
     * origin on the ground; the legs keep the feet where they stand.
     */
    public float weightRoll = 0F;

    public static class Leg
    {
        /** The upper and lower segments' bones. */
        public String upper;
        public String lower;
        /** The upper segment's pivot in the model (model units, +Y down, -Z forward). */
        public float[] hip;
        /** The lower segment's pivot relative to the upper's, at rest. */
        public float[] knee;
        /** The sole relative to the lower segment's pivot, at rest: the point put on the ground. */
        public float[] foot;
        /**
         * Which way the joint bends: 1 turns the lower segment by a positive angle about X (the
         * foot goes back, a knee), -1 by a negative one (an elbow). It never bends the other way
         * past its rest pose.
         */
        public float bend = 1F;
    }

}
