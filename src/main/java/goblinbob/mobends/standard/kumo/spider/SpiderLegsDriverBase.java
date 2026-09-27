package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.standard.data.SpiderData;

/** Shared plumbing of the spider leg drivers: bone slots, the kneel bounce, the reset hook, leg writes. */
public abstract class SpiderLegsDriverBase implements IPoseItem
{

    public static final int LIMBS = 8;

    protected final int[] upperSlots = new int[LIMBS];
    protected final int[] lowerSlots = new int[LIMBS];
    protected final String resetVariable;

    private final Quaternion yaw = new Quaternion();
    private final Quaternion bend = new Quaternion();
    private final Quaternion upper = new Quaternion();

    protected SpiderLegsDriverBase(Skeleton skeleton, String resetVariable) throws MalformedKumoTemplateException
    {
        for (int i = 0; i < LIMBS; i++)
        {
            upperSlots[i] = skeleton.indexOf("leg" + (i + 1));
            lowerSlots[i] = skeleton.indexOf("foreLeg" + (i + 1));
            if (upperSlots[i] < 0 || lowerSlots[i] < 0)
            {
                throw new MalformedKumoTemplateException("The spider leg drivers need bones leg1..8 and foreLeg1..8.");
            }
        }
        this.resetVariable = resetVariable;
    }

    protected static SpiderData subject(IKumoContext context)
    {
        return context.getSubject() instanceof SpiderData ? (SpiderData) context.getSubject() : null;
    }

    /** The landing bounce of the procedural bits: a damped sine over the touchdown progress. */
    protected static double kneelBounce(IKumoContext context, float duration, float amplitude, float lead)
    {
        float touchdown = Math.min((float) context.resolveVariable("ticksAfterTouchdown") / duration, 1.0F);
        if (touchdown >= 1.0F)
        {
            return 0;
        }
        float touchdownInv = 1.0F - touchdown;
        return Math.sin((touchdown * (1 + lead) - lead) * Math.PI * 2) * amplitude * touchdownInv;
    }

    /**
     * Writes one leg: the upper segment yawed then bent about its local Z, the lower segment bent
     * about Z ({@code orientY(yaw).localRotateZ(upper)} / {@code orientZ(lower)} in the bits).
     */
    protected void writeLeg(Pose pose, int index, boolean odd, float yawDegrees, double[] angles, float smoothness, boolean snap)
    {
        float side = odd ? -1F : 1F;
        PoseMath.axisAngleDegrees(0, 1, 0, yawDegrees, yaw);
        PoseMath.axisAngleDegrees(0, 0, 1, (float) (angles[0] / Math.PI * 180) * side, bend);
        Quaternion.mul(yaw, bend, upper);
        pose.composeRotation(upperSlots[index], upper, Pose.Space.OVERRIDE);
        PoseMath.axisAngleDegrees(0, 0, 1, (float) (angles[1] / Math.PI * 180) * side, bend);
        pose.composeRotation(lowerSlots[index], bend, Pose.Space.OVERRIDE);
        for (int slot : new int[] { upperSlots[index], lowerSlots[index] })
        {
            pose.get(slot).snap = snap;
            pose.get(slot).smoothness = smoothness;
        }
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        SpiderData data = subject(context);
        VariableScope layer = context.getLayerScope();
        if (data != null && layer != null && resetVariable != null && layer.has(resetVariable) && layer.get(resetVariable) != 0)
        {
            for (SpiderData.Limb limb : data.limbs)
            {
                limb.resetPosition();
            }
            layer.set(resetVariable, 0);
        }
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
