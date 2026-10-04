package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.standard.data.SpiderData;

import javax.annotation.Nullable;
import java.util.Map;

/** Shared plumbing of the spider leg drivers: bone slots, the kneel bounce, the reset hook, leg writes. */
public abstract class SpiderLegsDriverBase implements IPoseItem
{

    protected static final int LIMBS = SpiderData.LIMBS;

    protected final int[] upperSlots = new int[LIMBS];
    protected final int[] lowerSlots = new int[LIMBS];
    /** The state that, set, puts the legs back to rest when a node starts; null for none. */
    @Nullable
    protected final StateRef reset;
    private final VariableTable.Read ticksAfterTouchdown;
    /** The state the drivers publish the ground level in; null for none. */
    @Nullable
    private final StateRef groundLevelOut;

    private final Quaternion yaw = new Quaternion();
    private final Quaternion bend = new Quaternion();
    private final Quaternion upper = new Quaternion();
    /** Per-frame scratch for the IK. */
    protected final SpiderData.IKResult ik = new SpiderData.IKResult();
    protected final double[] angles = new double[2];

    protected SpiderLegsDriverBase(Skeleton skeleton, @Nullable String reset, @Nullable Map<String, String> out, ExpressionScope scope, String driver) throws MalformedKumoTemplateException
    {
        VariableTable variables = scope.getVariables();
        this.ticksAfterTouchdown = variables.read("entityTicksAfterTouchdown");
        if (out != null && !out.keySet().stream().allMatch("groundLevel"::equals))
        {
            throw new MalformedKumoTemplateException(driver + " has one output, 'groundLevel'.");
        }
        this.groundLevelOut = out == null || out.get("groundLevel") == null ? null : scope.resolveState(out.get("groundLevel"), driver + "'s output 'groundLevel'");
        for (int i = 0; i < LIMBS; i++)
        {
            upperSlots[i] = skeleton.indexOf("leg" + (i + 1));
            lowerSlots[i] = skeleton.indexOf("foreLeg" + (i + 1));
            if (upperSlots[i] < 0 || lowerSlots[i] < 0)
            {
                throw new MalformedKumoTemplateException("The spider leg drivers need bones leg1..8 and foreLeg1..8.");
            }
        }
        this.reset = reset == null ? null : scope.resolveState(reset, driver + "'s 'reset'");
    }

    protected static SpiderData subject(IKumoContext context)
    {
        return context.getSubject() instanceof SpiderData ? (SpiderData) context.getSubject() : null;
    }

    /** Publishes the ground level, if the driver has somewhere to. */
    protected void publishGroundLevel(double ground)
    {
        if (groundLevelOut != null)
        {
            groundLevelOut.set(ground);
        }
    }

    /** The landing bounce: a damped sine over the touchdown progress. */
    protected double kneelBounce(IKumoContext context, float duration, float amplitude, float lead)
    {
        float touchdown = Math.min((float) context.resolveVariable(ticksAfterTouchdown) / duration, 1.0F);
        if (touchdown >= 1.0F)
        {
            return 0;
        }
        float touchdownInv = 1.0F - touchdown;
        return Math.sin((touchdown * (1 + lead) - lead) * Math.PI * 2) * amplitude * touchdownInv;
    }

    /** Writes one leg: the upper segment yawed then bent about its local Z, the lower segment bent about Z. */
    protected void writeLeg(Pose pose, int index, boolean odd, float yawDegrees, double[] angles, float smoothness, boolean snap)
    {
        float side = odd ? -1F : 1F;
        PoseMath.axisAngleDegrees(0, 1, 0, yawDegrees, yaw);
        PoseMath.axisAngleDegrees(0, 0, 1, (float) (angles[0] / Math.PI * 180) * side, bend);
        Quaternion.mul(yaw, bend, upper);
        pose.composeRotation(upperSlots[index], upper, Pose.Space.OVERRIDE);
        PoseMath.axisAngleDegrees(0, 0, 1, (float) (angles[1] / Math.PI * 180) * side, bend);
        pose.composeRotation(lowerSlots[index], bend, Pose.Space.OVERRIDE);
        pose.get(upperSlots[index]).snap = snap;
        pose.get(upperSlots[index]).smoothness = smoothness;
        pose.get(lowerSlots[index]).snap = snap;
        pose.get(lowerSlots[index]).smoothness = smoothness;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        SpiderData data = subject(context);
        if (data != null && reset != null && reset.get(context) != 0)
        {
            for (SpiderData.Limb limb : data.limbs)
            {
                limb.resetPosition();
            }
            reset.set(0);
        }
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
