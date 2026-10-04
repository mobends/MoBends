package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.VectorTemplate;

/**
 * Writes a vector target (e.g. the global offset) from three expressions:
 * {@code {"core:vector": {"bone": "root", "y": {"mul": ["deep", 14]}}}}.
 * Its {@code when} is applied by the node, like every pose item's.
 */
public class VectorDriver implements IPoseItem
{
    /** An axis the template leaves out keeps the bone's current target for that axis. */
    private static final Expression UNWRITTEN = Expression.constant(Float.NaN);

    private final int slot;
    private final Expression x, y, z;
    private final Pose.Space space;
    private final ItemEffects effects;
    private final int[] written;

    public VectorDriver(int slot, Expression x, Expression y, Expression z, Pose.Space space, ItemEffects effects)
    {
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.z = z;
        this.space = space;
        this.effects = effects;
        this.written = new int[] { slot };
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, VectorTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:vector needs a 'bone'.");
        }
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.damping, template.vectorModes, template.snap);
        return new VectorDriver(skeleton.indexOf(template.bone),
                Expression.compile(template.x, context.getExpressionScope(), UNWRITTEN),
                Expression.compile(template.y, context.getExpressionScope(), UNWRITTEN),
                Expression.compile(template.z, context.getExpressionScope(), UNWRITTEN),
                template.space == null ? Pose.Space.OVERRIDE : template.space,
                effects.isEmpty() ? null : effects);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        pose.composeVector(slot, x.get(context), y.get(context), z.get(context), space);
        if (effects != null)
        {
            effects.apply(pose, written, context);
        }
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
