package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.StepTurnTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * See {@link StepTurnTemplate}. The feet are kept in world coordinates (blocks, yaw in degrees)
 * and brought into the model's frame each frame, turned by the yaw the body is shown at.
 *
 * <p>The model's frame relates to the world as vanilla's renderer puts it: a model point
 * {@code (x, z)} of a body at yaw {@code D} lies at {@code (x·cos D + z·sin D, x·sin D − z·cos D)}
 * from the entity (a reflection, so the same formula goes back), and turning a point about the
 * model's Y axis by {@code a} turns it by {@code a} of yaw. The rotation bone turns the model by
 * {@code bodyYaw − shownYaw} of yaw before vanilla turns it by {@code bodyYaw}.
 *
 * <p>The weight roll turns the model about the Z axis through the entity's origin, which in the
 * model lies {@link #ORIGIN_Y} units down (vanilla draws the model 1.501 blocks above it). In the
 * rotation bone's frame, where the model's Y and Z are flipped, a roll of {@code r} is one of
 * {@code −r}. The rotation bone is applied before the offset bone, so the offset is written turned
 * back by what lies beneath in the rotation bone (an animator's counter-rotation of vanilla's own
 * rocking) to end up where it is meant in the shown frame.
 */
public class StepTurnDriver implements IPoseItem
{

    public static final String TURN_LAG = "turnLag";
    public static final String TURN_SPEED = "turnSpeed";
    public static final String STEP_LIFT = "stepLift";
    public static final String STEP_IMPACT = "stepImpact";
    public static final String STRIDE = "stride";

    /** The entity's origin in the model, in model units down from the model's own. */
    private static final float ORIGIN_Y = 1.501F * 16F;
    /** Model units per tick under which the entity counts as standing, and the feet wait for the body to settle. */
    private static final double MOVING_SPEED = 0.1;

    /** The simulation step, in ticks. */
    private static final float MAX_STEP = 0.25F;
    /** The most ticks one frame simulates, so a hitch doesn't run the simulation for long. */
    private static final float MAX_FRAME = 5F;
    /** A step reaches its spot this far through, then only comes down. */
    private static final float REACH_AT = 0.85F;
    /** Until this far through a step, the foot keeps aiming for where the body is turning to now. */
    private static final float RETARGET_UNTIL = 0.5F;
    /** How much a step is favoured when its leg is on the side of the turn. */
    private static final double LEAD_BIAS = 0.5;
    /** How much of a leg's length it stretches to before a step in the air hurries to relieve it. */
    private static final double REACH = 0.97;
    /** The share of the run speed from which the gait eases into running. */
    private static final double RUN_FROM = 0.8;
    /** The fewest ticks a hurried step still has to go. */
    private static final float MIN_REMAINING = 1F;
    /** The shortest a hurried step gets, as a share of the shortest step. */
    private static final float MIN_HURRIED = 0.6F;
    /** Ticks over which the hips ease down into a walking crouch. */
    private static final float CROUCH_DROP = 1F;
    /** Ticks over which the hips ease back up from a walking crouch. */
    private static final float CROUCH_EASE = 6F;

    private final StepTurnTemplate t;
    private final Expression weight;
    private final int rotationSlot;
    private final int offsetSlot;
    private final Leg[] legs;

    /** Whether the feet stand somewhere; false while the weight is 0, so they're placed anew under the body. */
    private boolean placed;
    /** Whether anything was written last frame, so the frame the weight reaches 0 hands the offset and rotation back. */
    /** The body yaw and the position it follows. */
    private final VariableTable.Read yaw, x, z;
    /** The node variables it publishes (see {@link #STRIDE} and the others). */
    private final int strideOut, turnLagOut, turnSpeedOut, stepLiftOut, stepImpactOut;
    private boolean wrote;
    /** The yaw the body is shown at, and how fast it turns (degrees, degrees per tick). */
    private double shownYaw;
    private double turnSpeed;
    /** The leg in the air, or -1. */
    private int stepping = -1;
    private float sinceLanding = Float.MAX_VALUE;
    /** How far into its dip the body is after the landings, and how fast it goes. */
    private float impact, impactVelocity;
    /** The entity's smoothed velocity (blocks per tick), and where it was last frame. */
    private double velocityX, velocityZ, lastX, lastZ;
    /** The entity's smoothed speed, in blocks per tick. */
    private double speed;
    /** The entity's velocity smoothed over {@code runFacingSmoothing}: the way the body faces running. */
    private double headingX, headingZ;
    private boolean hasLast;
    /** How far the hips are down past the crouch, for reach (model units). */
    private float walkCrouch;

    private final Quaternion upper = new Quaternion();
    private final Quaternion lower = new Quaternion();
    private final Quaternion below = new Quaternion();
    private final Quaternion blended = new Quaternion();
    private final Quaternion turn = new Quaternion();
    private final Quaternion roll = new Quaternion();
    private final Vec3f offsetBelow = new Vec3f();

    private static class Leg
    {
        final StepTurnTemplate.Leg def;
        final int upperSlot;
        final int lowerSlot;
        /** The sole at rest, in the model. */
        final float restX, restY, restZ;
        /** 1 for a leg on the +X side of the model, -1 on the -X side. */
        final float side;
        /** Hip to sole at rest: as far as the leg reaches, the joint never opening past its rest bend. */
        final double length;

        /** Where the foot stands, in the world, and which way it points. */
        double x, z, yaw;
        /** While in the air: where it lifted from and where it goes. */
        double fromX, fromZ, fromYaw, toX, toZ, toYaw;
        float progress;
        /** Ticks the current step takes. */
        float duration;
        /** Model units the foot is ahead of its rest this frame. */
        float forward;

        Leg(StepTurnTemplate.Leg def, int upperSlot, int lowerSlot)
        {
            this.def = def;
            this.upperSlot = upperSlot;
            this.lowerSlot = lowerSlot;
            this.restX = def.hip[0] + def.knee[0] + def.foot[0];
            this.restY = def.hip[1] + def.knee[1] + def.foot[1];
            this.restZ = def.hip[2] + def.knee[2] + def.foot[2];
            this.side = restX >= 0 ? 1F : -1F;
            final float lx = def.knee[0] + def.foot[0], ly = def.knee[1] + def.foot[1], lz = def.knee[2] + def.foot[2];
            this.length = Math.sqrt(lx * lx + ly * ly + lz * lz);
        }
    }

    public StepTurnDriver(Skeleton skeleton, StepTurnTemplate template, Expression weight, VariableTable variables) throws MalformedKumoTemplateException
    {
        this.t = template;
        this.weight = weight;
        this.yaw = variables.read(template.yawVariable);
        this.x = variables.read(template.xVariable);
        this.z = variables.read(template.zVariable);
        this.strideOut = variables.nodeVariable(STRIDE);
        this.turnLagOut = variables.nodeVariable(TURN_LAG);
        this.turnSpeedOut = variables.nodeVariable(TURN_SPEED);
        this.stepLiftOut = variables.nodeVariable(STEP_LIFT);
        this.stepImpactOut = variables.nodeVariable(STEP_IMPACT);
        this.rotationSlot = skeleton.indexOf(template.rotationBone);
        this.offsetSlot = skeleton.indexOf(template.offsetBone);
        this.legs = new Leg[template.legs.size()];
        for (int i = 0; i < legs.length; i++)
        {
            StepTurnTemplate.Leg def = template.legs.get(i);
            legs[i] = new Leg(def, skeleton.indexOf(def.upper), skeleton.indexOf(def.lower));
        }
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, StepTurnTemplate template) throws MalformedKumoTemplateException
    {
        if (template.legs == null || template.legs.isEmpty())
        {
            throw new MalformedKumoTemplateException("core:step_turn needs 'legs'.");
        }
        for (StepTurnTemplate.Leg leg : template.legs)
        {
            if (leg.upper == null || leg.lower == null || !isVector(leg.hip) || !isVector(leg.knee) || !isVector(leg.foot))
            {
                throw new MalformedKumoTemplateException("core:step_turn: every leg needs 'upper' and 'lower' bones and 'hip', 'knee' and 'foot' as [x, y, z].");
            }
        }
        if (template.rotationBone == null || template.offsetBone == null || !Skeleton.isVectorBone(template.offsetBone))
        {
            throw new MalformedKumoTemplateException("core:step_turn needs a 'rotationBone' and an 'offsetBone' that is an offset vector (localOffset).");
        }
        if (template.stepDuration <= 0 || template.turnThreshold <= 0 || template.driftThreshold <= 0 || template.unitsPerBlock <= 0 || template.impactTime <= 0
                || template.strideLength <= 0 || template.runStrideLength <= 0 || template.minStepDuration <= 0 || template.velocitySmoothing <= 0 || template.runFacingSmoothing <= 0)
        {
            throw new MalformedKumoTemplateException("core:step_turn: 'stepDuration', 'turnThreshold', 'driftThreshold', 'unitsPerBlock', 'impactTime', 'strideLength', 'runStrideLength', 'minStepDuration', 'velocitySmoothing' and 'runFacingSmoothing' must be positive.");
        }
        return new StepTurnDriver(skeleton, template, Expression.compile(template.weight, context.getExpressionScope(), Expression.ONE), context.getExpressionScope().getVariables());
    }

    private static boolean isVector(float[] v)
    {
        return v != null && v.length == 3;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        final float w = Math.max(0F, Math.min(1F, weight.get(context)));
        final double bodyYaw = context.resolveVariable(yaw);
        final double px = context.resolveVariable(x);
        final double pz = context.resolveVariable(z);
        measureVelocity(px, pz, context.getDeltaTime());

        if (w <= 0)
        {
            placed = false;
            if (wrote)
            {
                // Hand the whole-model bones back as they are beneath.
                pose.rotationSoFar(rotationSlot, below);
                pose.composeRotation(rotationSlot, below, Pose.Space.OVERRIDE);
                pose.vectorSoFar(offsetSlot, offsetBelow);
                pose.composeVector(offsetSlot, offsetBelow.x, offsetBelow.y, offsetBelow.z, Pose.Space.OVERRIDE);
                wrote = false;
            }
            publish(context.getNodeScope(), 0, 0, 0, 0, 0);
            return;
        }

        if (!placed || tooFar(px, pz))
        {
            place(bodyYaw, px, pz);
        }
        for (float remaining = Math.min(context.getDeltaTime(), MAX_FRAME); remaining > 0; remaining -= MAX_STEP)
        {
            simulate(Math.min(remaining, MAX_STEP), bodyYaw, px, pz);
        }
        updateWalkCrouch(px, pz, context.getDeltaTime());

        // The hips: lowered, dipping after a landing, shifted and rolled over the planted feet while one is up.
        final float impact = this.impact;
        float lift = 0F;
        float shift = 0F;
        if (stepping >= 0)
        {
            Leg leg = legs[stepping];
            lift = liftCurve(leg.progress) * leg.side;
            shift = -leg.side * (float) Math.sin(Math.PI * leg.progress);
        }
        final float hipX = t.weightShift * shift * w;
        final float hipY = (t.crouch + walkCrouch + t.impactDepth * impact) * w;
        final double rollRad = Math.toRadians(t.weightRoll * shift * w);
        final double cosR = Math.cos(rollRad), sinR = Math.sin(rollRad);

        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        float stride = 0F;
        for (int i = 0; i < legs.length; i++)
        {
            poseLeg(pose, legs[i], i == stepping, px, pz, cosD, sinD, hipX, hipY, cosR, sinR, w);
            stride += legs[i].side * legs[i].forward;
        }
        stride /= legs.length;

        // Vanilla turns the model by bodyYaw; turn it back to the shown yaw first, then roll it.
        final float lag = (float) wrapDegrees(bodyYaw - shownYaw);
        PoseMath.axisAngleDegrees(0, 1, 0, lag * w, turn);
        PoseMath.axisAngleDegrees(0, 0, 1, (float) -Math.toDegrees(rollRad), roll);
        pose.rotationSoFar(rotationSlot, below);
        Quaternion.mul(turn, roll, blended);
        Quaternion.mul(blended, below, blended);
        pose.composeRotation(rotationSlot, blended, Pose.Space.OVERRIDE);
        pose.get(rotationSlot).snap = true;

        // The offset is in the shown body's frame, where the model's Y and Z are flipped, turned
        // back by what lies beneath in the rotation bone (applied to it before this rotation's roll).
        final double[] hip = rotateInverse(below, hipX, -hipY, 0);
        pose.vectorSoFar(offsetSlot, offsetBelow);
        pose.composeVector(offsetSlot, offsetBelow.x + (float) hip[0], offsetBelow.y + (float) hip[1], offsetBelow.z + (float) hip[2], Pose.Space.OVERRIDE);
        pose.get(offsetSlot).vectorMode = IVectorSink.Mode.SNAP;
        wrote = true;

        publish(context.getNodeScope(), lag * w, (float) turnSpeed * w, lift * w, impact * w, stride * w);
    }

    /** {@code v} turned by the inverse of {@code q}. */
    private static double[] rotateInverse(Quaternion q, double x, double y, double z)
    {
        // v + 2w(u × v) + 2u × (u × v), with u = −(q.x, q.y, q.z)
        final double ux = -q.x, uy = -q.y, uz = -q.z;
        final double cx = uy * z - uz * y, cy = uz * x - ux * z, cz = ux * y - uy * x;
        final double ccx = uy * cz - uz * cy, ccy = uz * cx - ux * cz, ccz = ux * cy - uy * cx;
        return new double[] {x + 2 * (q.w * cx + ccx), y + 2 * (q.w * cy + ccy), z + 2 * (q.w * cz + ccz)};
    }

    /** Follows the entity's velocity from its position frame to frame, smoothed. */
    private void measureVelocity(double px, double pz, float dt)
    {
        if (hasLast && dt > 0)
        {
            final double k = Math.min(1.0, dt / t.velocitySmoothing);
            final double rawX = (px - lastX) / dt, rawZ = (pz - lastZ) / dt;
            velocityX += (rawX - velocityX) * k;
            velocityZ += (rawZ - velocityZ) * k;
            // Smoothed on its own: the smoothed velocity shrinks while the entity turns.
            speed += (Math.hypot(rawX, rawZ) - speed) * k;
            // The way it runs, smoothed more: a path's zigzag between blocks averages out.
            final double h = Math.min(1.0, dt / t.runFacingSmoothing);
            headingX += (rawX - headingX) * h;
            headingZ += (rawZ - headingZ) * h;
        }
        lastX = px;
        lastZ = pz;
        hasLast = true;
    }

    /** The entity's speed, in model units per tick. */
    private double speed()
    {
        return speed * t.unitsPerBlock;
    }

    /**
     * Ticks a step takes at the entity's speed: as long as it may, but short enough that the stride
     * stays within its length. The pause after it shortens in proportion ({@link #pauseNow()}).
     */
    private float stepDurationNow()
    {
        final double speed = speed();
        if (speed <= 1e-4)
        {
            return t.stepDuration;
        }
        // Over a cycle of two steps and two pauses, a foot travels as far as the body does.
        final double duration = strideLength() / (2 * speed * (1 + pauseRatio()));
        return (float) Math.max(t.minStepDuration, Math.min(t.stepDuration, duration));
    }

    /** 0 up to {@link #RUN_FROM} of the run speed, easing to 1 at it. */
    private float running()
    {
        if (t.runSpeed <= 0)
        {
            return 0F;
        }
        final double x = Math.max(0, Math.min(1, (speed() / t.unitsPerBlock / t.runSpeed - RUN_FROM) / (1 - RUN_FROM)));
        return (float) smooth(x);
    }

    /** The farthest a foot travels a step: longer running. */
    private float strideLength()
    {
        return t.strideLength + (t.runStrideLength - t.strideLength) * running();
    }

    /** How long the pause after a step is to the step: none running. */
    private float pauseRatio()
    {
        return t.stepPause / t.stepDuration * (1F - running());
    }

    /**
     * The yaw the body is to face: the entity's own, or, running, the way it goes (vanilla's yaw
     * 0 faces +Z, and +X is to the left).
     */
    private double facingYaw(double bodyYaw)
    {
        final float run = running();
        if (run <= 0)
        {
            return bodyYaw;
        }
        final double moveYaw = Math.toDegrees(Math.atan2(-headingX, headingZ));
        return bodyYaw + run * wrapDegrees(moveYaw - bodyYaw);
    }

    private float pauseNow()
    {
        return pauseRatio() * stepDurationNow();
    }

    /** 0 standing, 1 once the steps have to quicken to keep the stride. */
    private float walking()
    {
        final double quicken = t.strideLength / (2 * (t.stepDuration + t.stepPause));
        return (float) Math.min(1.0, speed() / quicken);
    }

    /**
     * Ticks ahead of now the body is where a foot landing {@code untilLanding} ticks from now should
     * stand under it: half the time it then stays planted, so it lands as far ahead as it lifts behind.
     */
    private float leadTicks(float untilLanding)
    {
        return untilLanding + (stepDurationNow() + 2 * pauseNow()) * 0.5F;
    }

    private void poseLeg(Pose pose, Leg leg, boolean inAir, double px, double pz, double cosD, double sinD, float hipX, float hipY, double cosR, double sinR, float w)
    {
        double footX = leg.x, footZ = leg.z, footYaw = leg.yaw, footLift = 0;
        if (inAir)
        {
            final double along = smooth(Math.min(1F, leg.progress / REACH_AT));
            footX = leg.fromX + (leg.toX - leg.fromX) * along;
            footZ = leg.fromZ + (leg.toZ - leg.fromZ) * along;
            footYaw = leg.fromYaw + wrapDegrees(leg.toYaw - leg.fromYaw) * along;
            footLift = (t.stepHeight + (t.runStepHeight - t.stepHeight) * running()) * liftCurve(leg.progress);
        }
        // The foot in the model, relative to the (shifted, lowered) hip.
        final double wx = (footX - px) * t.unitsPerBlock;
        final double wz = (footZ - pz) * t.unitsPerBlock;
        final double mx = wx * cosD + wz * sinD;
        final double mz = wx * sinD - wz * cosD;
        final double my = leg.restY - footLift;
        leg.forward = (float) (leg.restZ - mz);
        // Back into the model: unrolled about the entity's origin, then off the shifted, lowered hips.
        final double ry = my - ORIGIN_Y;
        final double ux = mx * cosR + ry * sinR;
        final double uy = -mx * sinR + ry * cosR + ORIGIN_Y;
        final float[] hip = leg.def.hip;
        LegIk.solve(leg.def.knee, leg.def.foot, leg.def.bend, Math.toRadians(wrapDegrees(footYaw - shownYaw)),
                ux - (hip[0] + hipX), uy - (hip[1] + hipY), mz - hip[2], upper, lower);
        writeBlended(pose, leg.upperSlot, upper, w);
        writeBlended(pose, leg.lowerSlot, lower, w);
    }

    /** At full weight the bone snaps to the IK, so planted feet don't slide; below it, it blends from what's beneath with the bone's damping. */
    private void writeBlended(Pose pose, int slot, Quaternion rotation, float w)
    {
        if (w >= 1F)
        {
            pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
            pose.get(slot).snap = true;
            return;
        }
        pose.rotationSoFar(slot, below);
        PoseMath.slerp(below, rotation, w, blended);
        pose.composeRotation(slot, blended, Pose.Space.OVERRIDE);
    }

    private void simulate(float dt, double bodyYaw, double px, double pz)
    {
        final double facing = facingYaw(bodyYaw);
        sinceLanding = Math.min(sinceLanding + dt, 1e6F);
        advanceImpact(dt);
        if (stepping >= 0)
        {
            Leg leg = legs[stepping];
            if (leg.progress < RETARGET_UNTIL)
            {
                // A step taken as the entity sets off hurries once it's going faster.
                leg.duration = Math.min(leg.duration, stepDurationNow());
                aim(leg, goalYaw(facing), px, pz, leadTicks((1F - leg.progress) * leg.duration));
            }
            // It hurries before a planted foot is left out of the leg's reach (setting off, the
            // other foot has stood still under the body all this step).
            final float remaining = (1F - leg.progress) * leg.duration;
            // Never quicker than a share of the shortest step: faster than the legs can keep up
            // with, the planted foot drags rather than the feet flickering.
            final float untilStretched = Math.max(MIN_REMAINING, untilStretched(px, pz));
            if (untilStretched < remaining)
            {
                leg.duration = Math.max(t.minStepDuration * MIN_HURRIED, untilStretched / (1F - leg.progress));
            }
            leg.progress += dt / leg.duration;
            if (leg.progress >= 1F)
            {
                leg.x = leg.toX;
                leg.z = leg.toZ;
                leg.yaw = leg.toYaw;
                stepping = -1;
                sinceLanding = 0F;
                impactVelocity += (float) Math.E / t.impactTime;
            }
        }

        // The body turns towards where its feet point (a foot in the air counts where it lifted from);
        // running, the way it goes, the feet catching up with the next steps.
        final float run = running();
        final double feetYaw = feetYaw();
        final double error = wrapDegrees(feetYaw + run * wrapDegrees(facing - feetYaw) - shownYaw);
        final double stiffness = t.bodyStiffness + (t.runBodyStiffness - t.bodyStiffness) * run;
        final double damping = t.bodyDamping + (t.runBodyDamping - t.bodyDamping) * run;
        turnSpeed += (error * stiffness - turnSpeed * damping) * dt;
        shownYaw = wrapDegrees(shownYaw + turnSpeed * dt);

        // Walking, the feet don't wait for the body to settle.
        final boolean settled = speed() > MOVING_SPEED || Math.abs(wrapDegrees(feetYaw() - shownYaw)) <= t.settleAngle;
        if (stepping < 0 && sinceLanding >= pauseNow() && settled)
        {
            startStep(facing, px, pz);
        }
    }

    /** Lifts the foot furthest from where it should be, favouring the side the body turns to; none if every foot is close enough. */
    private void startStep(double facing, double px, double pz)
    {
        final double lead = wrapDegrees(facing - shownYaw);
        final double goalYaw = goalYaw(facing);
        // Turning to a higher yaw turns right, so the leg on the -X (right) side leads.
        final float leadSide = lead > 0 ? -1F : 1F;
        // Right after a step, the feet square up to a smaller error.
        final boolean finishing = sinceLanding < t.finishWindow;
        final float turnThreshold = finishing ? Math.min(t.finishThreshold, t.turnThreshold) : t.turnThreshold;
        final float driftThreshold = finishing ? Math.min(t.finishDrift, t.driftThreshold) : t.driftThreshold;
        final float duration = stepDurationNow();
        final float leadTime = leadTicks(duration);

        int best = -1;
        double bestScore = 0;
        for (int i = 0; i < legs.length; i++)
        {
            Leg leg = legs[i];
            aim(leg, goalYaw, px, pz, leadTime);
            final double distance = Math.hypot(leg.toX - leg.x, leg.toZ - leg.z) * t.unitsPerBlock;
            final double turnError = Math.abs(wrapDegrees(goalYaw - leg.yaw));
            double score = Math.max(turnError / turnThreshold, distance / driftThreshold);
            if (score < 1)
            {
                continue;
            }
            if (leg.side == leadSide)
            {
                score += LEAD_BIAS;
            }
            if (score > bestScore)
            {
                bestScore = score;
                best = i;
            }
        }
        if (best < 0)
        {
            return;
        }
        Leg leg = legs[best];
        leg.fromX = leg.x;
        leg.fromZ = leg.z;
        leg.fromYaw = leg.yaw;
        leg.progress = 0F;
        leg.duration = duration;
        stepping = best;
    }

    /**
     * Model units the hips go down further, up to {@link StepTurnTemplate#walkCrouch}: that much
     * while walking, and as much as the planted feet need to stay within reach (a walk ends with
     * them apart), easing back up.
     */
    private void updateWalkCrouch(double px, double pz, float dt)
    {
        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        double needed = t.walkCrouch * walking();
        for (int i = 0; i < legs.length; i++)
        {
            if (i == stepping)
            {
                continue;
            }
            final Leg leg = legs[i];
            final double wx = (leg.x - px) * t.unitsPerBlock, wz = (leg.z - pz) * t.unitsPerBlock;
            final double dx = wx * cosD + wz * sinD - leg.def.hip[0];
            final double dz = wx * sinD - wz * cosD - leg.def.hip[2];
            final double reach = leg.length * REACH;
            // The highest the hip may be over the ground for the leg to reach its foot.
            final double height = Math.sqrt(Math.max(0, reach * reach - dx * dx - dz * dz));
            needed = Math.max(needed, leg.restY - leg.def.hip[1] - t.crouch - height);
        }
        final float target = (float) Math.min(t.walkCrouch, needed);
        walkCrouch += (target - walkCrouch) * Math.min(1F, dt / (target > walkCrouch ? CROUCH_DROP : CROUCH_EASE));
    }

    /** Ticks until the moving body leaves a planted foot out of its leg's reach, as far as can be told from its speed. */
    private float untilStretched(double px, double pz)
    {
        final double speed = speed();
        final double velocity = Math.hypot(velocityX, velocityZ);
        if (speed < 1e-3 || velocity < 1e-6)
        {
            return Float.MAX_VALUE;
        }
        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        // As low as the hips go for reach.
        final float hipY = t.crouch + t.walkCrouch;
        // Which way the body goes, in the model.
        final double forwardX = (velocityX * cosD + velocityZ * sinD) / velocity;
        final double forwardZ = (velocityX * sinD - velocityZ * cosD) / velocity;
        double until = Double.MAX_VALUE;
        for (int i = 0; i < legs.length; i++)
        {
            if (i == stepping)
            {
                continue;
            }
            final Leg leg = legs[i];
            final double wx = (leg.x - px) * t.unitsPerBlock, wz = (leg.z - pz) * t.unitsPerBlock;
            final double dx = wx * cosD + wz * sinD - leg.def.hip[0];
            final double dz = wx * sinD - wz * cosD - leg.def.hip[2];
            final double height = leg.restY - (leg.def.hip[1] + hipY);
            final double reach = Math.sqrt(Math.max(0, leg.length * REACH * leg.length * REACH - height * height));
            // The foot goes back past the hip at the body's speed: when does it leave the reach?
            final double along = dx * forwardX + dz * forwardZ;
            final double outside = dx * dx + dz * dz - reach * reach;
            until = Math.min(until, outside >= 0 ? 0 : (along + Math.sqrt(along * along - outside)) / speed);
        }
        return (float) until;
    }

    /** Where a foot goes next: towards the entity's body yaw, at most a step's angle past the shown body. */
    private double goalYaw(double facing)
    {
        return shownYaw + Math.max(-t.maxStepAngle, Math.min(t.maxStepAngle, wrapDegrees(facing - shownYaw)));
    }

    /**
     * Sets where a foot goes to stand at rest under a body at {@code yaw}, where the body will be in
     * {@code leadTicks} ticks at its velocity (at most a stride ahead).
     */
    private void aim(Leg leg, double yaw, double px, double pz, float leadTicks)
    {
        final double yawRad = Math.toRadians(yaw);
        final double cosG = Math.cos(yawRad), sinG = Math.sin(yawRad);
        double aheadX = velocityX * leadTicks, aheadZ = velocityZ * leadTicks;
        final double ahead = Math.hypot(aheadX, aheadZ) * t.unitsPerBlock;
        final float stride = strideLength();
        if (ahead > stride)
        {
            aheadX *= stride / ahead;
            aheadZ *= stride / ahead;
        }
        leg.toX = px + aheadX + (leg.restX * cosG + leg.restZ * sinG) / t.unitsPerBlock;
        leg.toZ = pz + aheadZ + (leg.restX * sinG - leg.restZ * cosG) / t.unitsPerBlock;
        leg.toYaw = yaw;
    }

    /** The mean of where the feet point, around the shown yaw. */
    private double feetYaw()
    {
        double sum = 0;
        for (Leg leg : legs)
        {
            sum += wrapDegrees(leg.yaw - shownYaw);
        }
        return shownYaw + sum / legs.length;
    }

    /** Puts every foot at rest under a body shown at the entity's own yaw. */
    private void place(double bodyYaw, double px, double pz)
    {
        shownYaw = wrapDegrees(bodyYaw);
        turnSpeed = 0;
        stepping = -1;
        sinceLanding = Float.MAX_VALUE;
        impact = 0F;
        impactVelocity = 0F;
        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        for (Leg leg : legs)
        {
            leg.x = px + (leg.restX * cosD + leg.restZ * sinD) / t.unitsPerBlock;
            leg.z = pz + (leg.restX * sinD - leg.restZ * cosD) / t.unitsPerBlock;
            leg.yaw = shownYaw;
        }
        placed = true;
    }

    /** Whether a planted foot got so far from under the body that stepping back makes no sense. */
    private boolean tooFar(double px, double pz)
    {
        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        for (int i = 0; i < legs.length; i++)
        {
            if (i == stepping)
            {
                // Already on its way.
                continue;
            }
            final Leg leg = legs[i];
            final double restX = px + (leg.restX * cosD + leg.restZ * sinD) / t.unitsPerBlock;
            final double restZ = pz + (leg.restX * sinD - leg.restZ * cosD) / t.unitsPerBlock;
            if (Math.hypot(restX - leg.x, restZ - leg.z) * t.unitsPerBlock > t.resetDistance)
            {
                return true;
            }
        }
        return false;
    }

    /** 0 at landing, 1 at {@code impactTime} ticks after it, then fading. */
    /**
     * The dip, critically damped: kicked by each landing so that alone it rises to 1 at
     * {@code impactTime} ticks and fades (x·e^(1−x)); landings close together add up smoothly
     * instead of starting it over.
     */
    private void advanceImpact(float dt)
    {
        final float rate = 1F / t.impactTime;
        impactVelocity += (-rate * rate * impact - 2 * rate * impactVelocity) * dt;
        impact += impactVelocity * dt;
    }

    /** The foot's height over a step, 0..1: lifted slowly, brought down hard. */
    private static float liftCurve(float progress)
    {
        return (float) Math.sin(Math.PI * Math.pow(Math.max(0F, Math.min(1F, progress)), 1.4));
    }

    private static double smooth(double x)
    {
        return x * x * (3 - 2 * x);
    }

    private void publish(VariableScope scope, float lag, float speed, float lift, float impact, float stride)
    {
        scope.set(strideOut, stride);
        scope.set(turnLagOut, lag);
        scope.set(turnSpeedOut, speed);
        scope.set(stepLiftOut, lift);
        scope.set(stepImpactOut, impact);
    }

    private static double wrapDegrees(double degrees)
    {
        degrees %= 360;
        if (degrees >= 180) degrees -= 360;
        if (degrees < -180) degrees += 360;
        return degrees;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        placed = false;
        publish(context.getNodeScope(), 0, 0, 0, 0, 0);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
