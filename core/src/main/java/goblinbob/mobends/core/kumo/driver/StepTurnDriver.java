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
 */
public class StepTurnDriver implements IPoseItem
{

    public static final String TURN_LAG = "turnLag";
    public static final String TURN_SPEED = "turnSpeed";
    public static final String STEP_LIFT = "stepLift";
    public static final String STEP_IMPACT = "stepImpact";

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

    private final StepTurnTemplate t;
    private final Expression weight;
    private final int rotationSlot;
    private final int offsetSlot;
    private final Leg[] legs;

    /** Whether the feet stand somewhere; false while the weight is 0, so they're placed anew under the body. */
    private boolean placed;
    /** Whether anything was written last frame, so the frame the weight reaches 0 hands the offset and rotation back. */
    private boolean wrote;
    /** The yaw the body is shown at, and how fast it turns (degrees, degrees per tick). */
    private double shownYaw;
    private double turnSpeed;
    /** The leg in the air, or -1. */
    private int stepping = -1;
    private float sinceLanding = Float.MAX_VALUE;

    private final Quaternion upper = new Quaternion();
    private final Quaternion lower = new Quaternion();
    private final Quaternion below = new Quaternion();
    private final Quaternion blended = new Quaternion();
    private final Quaternion turn = new Quaternion();
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

        /** Where the foot stands, in the world, and which way it points. */
        double x, z, yaw;
        /** While in the air: where it lifted from and where it goes. */
        double fromX, fromZ, fromYaw, toX, toZ, toYaw;
        float progress;

        Leg(StepTurnTemplate.Leg def, int upperSlot, int lowerSlot)
        {
            this.def = def;
            this.upperSlot = upperSlot;
            this.lowerSlot = lowerSlot;
            this.restX = def.hip[0] + def.knee[0] + def.foot[0];
            this.restY = def.hip[1] + def.knee[1] + def.foot[1];
            this.restZ = def.hip[2] + def.knee[2] + def.foot[2];
            this.side = restX >= 0 ? 1F : -1F;
        }
    }

    public StepTurnDriver(Skeleton skeleton, StepTurnTemplate template, Expression weight) throws MalformedKumoTemplateException
    {
        this.t = template;
        this.weight = weight;
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
        if (template.stepDuration <= 0 || template.turnThreshold <= 0 || template.driftThreshold <= 0 || template.unitsPerBlock <= 0 || template.impactTime <= 0)
        {
            throw new MalformedKumoTemplateException("core:step_turn: 'stepDuration', 'turnThreshold', 'driftThreshold', 'unitsPerBlock' and 'impactTime' must be positive.");
        }
        return new StepTurnDriver(skeleton, template, Expression.compile(template.weight, context.getExpressionScope(), Expression.ONE));
    }

    private static boolean isVector(float[] v)
    {
        return v != null && v.length == 3;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        final float w = Math.max(0F, Math.min(1F, weight.get(context)));
        final double bodyYaw = context.resolveVariable(t.yawVariable);
        final double px = context.resolveVariable(t.xVariable);
        final double pz = context.resolveVariable(t.zVariable);

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
            publish(context.getNodeScope(), 0, 0, 0, 0);
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

        // The hips: lowered, dipping after a landing, shifted over the planted feet while one is up.
        final float impact = impactEnvelope();
        float lift = 0F;
        float shiftX = 0F;
        if (stepping >= 0)
        {
            Leg leg = legs[stepping];
            lift = liftCurve(leg.progress) * leg.side;
            shiftX = -leg.side * t.weightShift * (float) Math.sin(Math.PI * leg.progress);
        }
        final float hipX = shiftX;
        final float hipY = t.crouch + t.impactDepth * impact;

        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        for (int i = 0; i < legs.length; i++)
        {
            poseLeg(pose, legs[i], i == stepping, px, pz, cosD, sinD, hipX, hipY, w);
        }

        // Vanilla turns the model by bodyYaw; turn it back to the shown yaw first.
        final float lag = (float) wrapDegrees(bodyYaw - shownYaw);
        PoseMath.axisAngleDegrees(0, 1, 0, lag * w, turn);
        pose.rotationSoFar(rotationSlot, below);
        Quaternion.mul(turn, below, blended);
        pose.composeRotation(rotationSlot, blended, Pose.Space.OVERRIDE);
        pose.get(rotationSlot).snap = true;

        // The offset is in the shown body's frame, where the model's Y and Z are flipped.
        pose.vectorSoFar(offsetSlot, offsetBelow);
        pose.composeVector(offsetSlot, offsetBelow.x + hipX * w, offsetBelow.y - hipY * w, offsetBelow.z, Pose.Space.OVERRIDE);
        pose.get(offsetSlot).vectorMode = IVectorSink.Mode.SNAP;
        wrote = true;

        publish(context.getNodeScope(), lag * w, (float) turnSpeed * w, lift * w, impact * w);
    }

    private void poseLeg(Pose pose, Leg leg, boolean inAir, double px, double pz, double cosD, double sinD, float hipX, float hipY, float w)
    {
        double footX = leg.x, footZ = leg.z, footYaw = leg.yaw, footLift = 0;
        if (inAir)
        {
            final double along = smooth(Math.min(1F, leg.progress / REACH_AT));
            footX = leg.fromX + (leg.toX - leg.fromX) * along;
            footZ = leg.fromZ + (leg.toZ - leg.fromZ) * along;
            footYaw = leg.fromYaw + wrapDegrees(leg.toYaw - leg.fromYaw) * along;
            footLift = t.stepHeight * liftCurve(leg.progress);
        }
        // The foot in the model, relative to the (shifted, lowered) hip.
        final double wx = (footX - px) * t.unitsPerBlock;
        final double wz = (footZ - pz) * t.unitsPerBlock;
        final double mx = wx * cosD + wz * sinD;
        final double mz = wx * sinD - wz * cosD;
        final double my = leg.restY - footLift;
        final float[] hip = leg.def.hip;
        LegIk.solve(leg.def.knee, leg.def.foot, leg.def.bend, Math.toRadians(wrapDegrees(footYaw - shownYaw)),
                mx - (hip[0] + hipX), my - (hip[1] + hipY), mz - hip[2], upper, lower);
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
        sinceLanding = Math.min(sinceLanding + dt, 1e6F);
        if (stepping >= 0)
        {
            Leg leg = legs[stepping];
            if (leg.progress < RETARGET_UNTIL)
            {
                aim(leg, goalYaw(bodyYaw), px, pz);
            }
            leg.progress += dt / t.stepDuration;
            if (leg.progress >= 1F)
            {
                leg.x = leg.toX;
                leg.z = leg.toZ;
                leg.yaw = leg.toYaw;
                stepping = -1;
                sinceLanding = 0F;
            }
        }

        // The body turns towards where its feet point (a foot in the air counts where it lifted from).
        final double error = wrapDegrees(feetYaw() - shownYaw);
        turnSpeed += (error * t.bodyStiffness - turnSpeed * t.bodyDamping) * dt;
        shownYaw = wrapDegrees(shownYaw + turnSpeed * dt);

        if (stepping < 0 && sinceLanding >= t.stepPause && Math.abs(wrapDegrees(feetYaw() - shownYaw)) <= t.settleAngle)
        {
            startStep(bodyYaw, px, pz);
        }
    }

    /** Lifts the foot furthest from where it should be, favouring the side the body turns to; none if every foot is close enough. */
    private void startStep(double bodyYaw, double px, double pz)
    {
        final double lead = wrapDegrees(bodyYaw - shownYaw);
        final double goalYaw = goalYaw(bodyYaw);
        // Turning to a higher yaw turns right, so the leg on the -X (right) side leads.
        final float leadSide = lead > 0 ? -1F : 1F;
        // Right after a step, the feet square up to a smaller error.
        final float turnThreshold = sinceLanding < t.finishWindow ? Math.min(t.finishThreshold, t.turnThreshold) : t.turnThreshold;

        int best = -1;
        double bestScore = 0;
        for (int i = 0; i < legs.length; i++)
        {
            Leg leg = legs[i];
            aim(leg, goalYaw, px, pz);
            final double distance = Math.hypot(leg.toX - leg.x, leg.toZ - leg.z) * t.unitsPerBlock;
            final double turnError = Math.abs(wrapDegrees(goalYaw - leg.yaw));
            double score = Math.max(turnError / turnThreshold, distance / t.driftThreshold);
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
        stepping = best;
    }

    /** Where a foot goes next: towards the entity's body yaw, at most a step's angle past the shown body. */
    private double goalYaw(double bodyYaw)
    {
        return shownYaw + Math.max(-t.maxStepAngle, Math.min(t.maxStepAngle, wrapDegrees(bodyYaw - shownYaw)));
    }

    /** Sets where a foot goes to stand at rest under a body at {@code yaw}. */
    private void aim(Leg leg, double yaw, double px, double pz)
    {
        final double yawRad = Math.toRadians(yaw);
        final double cosG = Math.cos(yawRad), sinG = Math.sin(yawRad);
        leg.toX = px + (leg.restX * cosG + leg.restZ * sinG) / t.unitsPerBlock;
        leg.toZ = pz + (leg.restX * sinG - leg.restZ * cosG) / t.unitsPerBlock;
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

    /** Whether a foot got so far from under the body that stepping back makes no sense. */
    private boolean tooFar(double px, double pz)
    {
        final double yawRad = Math.toRadians(shownYaw);
        final double cosD = Math.cos(yawRad), sinD = Math.sin(yawRad);
        for (Leg leg : legs)
        {
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
    private float impactEnvelope()
    {
        if (sinceLanding >= 1e6F)
        {
            return 0F;
        }
        final float x = sinceLanding / t.impactTime;
        return (float) (x * Math.exp(1 - x));
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

    private static void publish(VariableScope scope, float lag, float speed, float lift, float impact)
    {
        scope.set(TURN_LAG, lag);
        scope.set(TURN_SPEED, speed);
        scope.set(STEP_LIFT, lift);
        scope.set(STEP_IMPACT, impact);
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
        publish(context.getNodeScope(), 0, 0, 0, 0);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
