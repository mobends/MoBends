package goblinbob.mobends.standard.kumo.spider;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.pose.ValueSource;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.util.math.MathHelper;

/** See {@link SpiderMovingLegsTemplate}. {@code animateMovingLimb} of the spider's move and crawl bits. */
public class SpiderMovingLegsDriver extends SpiderLegsDriverBase
{

    private final SpiderMovingLegsTemplate t;
    private final ValueSource swing;
    private final ValueSource groundLevel;
    /** Never reset: the procedural bit eased its legs in once per entity, on its first play. */
    private float startTransition = 0F;

    public SpiderMovingLegsDriver(Skeleton skeleton, SpiderMovingLegsTemplate template) throws MalformedKumoTemplateException
    {
        super(skeleton, template.resetVariable);
        if (template.limbs == null || template.limbs.size() != LIMBS)
        {
            throw new MalformedKumoTemplateException("mobends:spider_moving_legs needs exactly 8 'limbs' entries.");
        }
        this.t = template;
        this.swing = ValueSource.fromTemplate(template.swing, ValueSource.ZERO);
        this.groundLevel = ValueSource.fromTemplate(template.groundLevel, ValueSource.ZERO);
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, SpiderMovingLegsTemplate template) throws MalformedKumoTemplateException
    {
        return new SpiderMovingLegsDriver(skeleton, template);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        SpiderData data = subject(context);
        if (data == null)
        {
            return;
        }
        final float clock = swing.get(context);
        float ground = groundLevel.get(context);
        if (t.kneelDuration != null)
        {
            ground += kneelBounce(context, t.kneelDuration, t.kneelAmplitude, t.kneelLead);
        }
        if (startTransition < 1.0F)
        {
            startTransition += context.getDeltaTime() * t.startSpeed;
        }

        for (int i = 0; i < LIMBS; i++)
        {
            SpiderMovingLegsTemplate.Limb p = t.limbs.get(i);
            final boolean odd = i % 2 == 1;
            final float offset = (i + 1) / 2 % 2 == 0 ? GUtil.PI : 0;
            final float phase = clock + p.phase;
            float sideRotation = p.minRot + (MathHelper.sin(phase + offset) * .5F + .5F) * (p.maxRot - p.minRot);
            float dist = p.minDist + (MathHelper.sin(phase + offset) * .5F + .5F) * (p.maxDist - p.minDist);
            float legGround = ground + -7 + Math.max(0, MathHelper.cos(phase + offset)) * 4;

            double[] angles = SpiderLegIk.solve(dist, legGround);
            boolean settled = startTransition >= 1.0F;
            writeLeg(pose, i, odd, odd ? sideRotation : -sideRotation, angles, settled ? 1F : startTransition, settled);

            data.limbs[i].setAngleAndDistance(odd ? sideRotation / 180F * GUtil.PI : GUtil.PI - sideRotation / 180F * GUtil.PI, dist * 0.0625F);
        }

        context.getNodeScope().set("groundLevel", ground);
    }

}
