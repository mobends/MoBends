package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.SpiderData;

/** See {@link SpiderIdleLegsTemplate}. */
public class SpiderIdleLegsDriver extends SpiderLegsDriverBase
{

    private final SpiderIdleLegsTemplate t;
    private final Expression groundLevel;
    private final Expression bodyX;
    private final Expression bodyZ;

    public SpiderIdleLegsDriver(Skeleton skeleton, SpiderIdleLegsTemplate template, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        super(skeleton, template.resetVariable);
        if (template.feelLimbs != null)
        {
            for (int limb : template.feelLimbs)
            {
                if (limb < 0 || limb >= LIMBS)
                {
                    throw new MalformedKumoTemplateException("mobends:spider_idle_legs: 'feelLimbs' are limb indices, 0 to " + (LIMBS - 1) + ".");
                }
            }
        }
        this.t = template;
        this.groundLevel = Expression.compile(template.groundLevel, scope, Expression.ZERO);
        this.bodyX = Expression.compile(template.bodyX, scope, Expression.ZERO);
        this.bodyZ = Expression.compile(template.bodyZ, scope, Expression.ZERO);
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SpiderIdleLegsTemplate template) throws MalformedKumoTemplateException
    {
        return new SpiderIdleLegsDriver(skeleton, template, context.getExpressionScope());
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        SpiderData data = subject(context);
        if (data == null)
        {
            return;
        }
        final float pt = (float) context.resolveVariable("partialTicks");
        double ground = groundLevel.get(context) + kneelBounce(context, t.kneelDuration, t.kneelAmplitude, t.kneelLead);
        final double bx = bodyX.get(context);
        final double bz = bodyZ.get(context);

        for (int i = 0; i < LIMBS; i++)
        {
            SpiderData.Limb limb = data.limbs[i];
            limb.solveIK(bx, bz, pt, ik);
            double deviation = GUtil.getRadianDifference(limb.getNeutralYaw(), ik.xzAngle + Math.PI / 2);
            if (deviation > 0.9 || ik.xzDistance * 0.0625 > 1.2)
            {
                limb.adjustToNeutralPosition();
            }
            double xzAngle = limb.isOdd() ? (Math.PI / 2 + ik.xzAngle) : (-Math.PI / 2 + ik.xzAngle);
            SpiderLegIk.solve(ik.xzDistance, ground + SpiderLegIk.REST_GROUND_LEVEL + Math.sin(limb.getAdjustingProgress() * Math.PI) * t.liftHeight, angles);
            writeLeg(pose, i, limb.isOdd(), (float) (xzAngle / Math.PI * 180F), angles, 1F, true);
        }

        // The front limbs reach out now and then, as if feeling the ground.
        if (t.feelLimbs != null && t.feelInterval > 0 && ((int) context.resolveVariable("ticksExisted")) % t.feelInterval < t.feelDuration)
        {
            for (int limb : t.feelLimbs)
            {
                data.limbs[limb].adjustToLocalPosition(t.feelX, t.feelZ, t.feelSpeed);
            }
        }

        context.getNodeScope().set("groundLevel", ground);
    }

}
