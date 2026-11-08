package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.ScaleTemplate;

/**
 * Scales a part from expressions, per axis of the part's own frame (1: as modelled):
 * {@code {"core:scale": {"bone": "head", "all": 1.5}}}. What the part carries (its children, a held
 * item, its armor) is scaled with it. Like {@code core:rotate}, it composes in {@code pre} space
 * unless told otherwise: there (and in {@code post}) it multiplies the part's scale, in
 * {@code override} it replaces it. Scales aren't smoothed. Its {@code when} is applied by the node,
 * like every pose item's.
 */
public class ScaleDriver implements IPoseItem
{
    /** An axis an OVERRIDE write leaves out keeps the part's current scale on that axis. */
    private static final Expression UNWRITTEN = Expression.constant(Float.NaN);

    private final int slot;
    private final Expression x, y, z;
    private final Pose.Space space;

    public ScaleDriver(int slot, Expression x, Expression y, Expression z, Pose.Space space)
    {
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.z = z;
        this.space = space;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, ScaleTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:scale needs a 'bone'.");
        }
        if (Skeleton.isEntityBone(template.bone))
        {
            throw new MalformedKumoTemplateException("core:scale scales parts; '" + template.bone + "' is one of the entity's own bones, which can't be scaled.");
        }
        if (template.smoothing != null || template.vectorModes != null || template.snap)
        {
            throw new MalformedKumoTemplateException("core:scale isn't smoothed: it takes no '@smoothing', '@vectorModes' or '@snap'.");
        }
        Pose.Space space = template.space == null ? Pose.Space.PRE : template.space;
        ExpressionScope scope = context.getExpressionScope();
        // A relative write that leaves an axis out scales it by one.
        Expression unwritten = space == Pose.Space.OVERRIDE ? UNWRITTEN : Expression.ONE;
        Expression all = Expression.compile(template.all, scope, unwritten);
        return new ScaleDriver(skeleton.indexOf(template.bone),
                Expression.compile(template.x, scope, all),
                Expression.compile(template.y, scope, all),
                Expression.compile(template.z, scope, all),
                space);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        pose.composeScale(slot, x.get(context), y.get(context), z.get(context), space);
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

}
