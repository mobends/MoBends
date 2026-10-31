package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.TranslateTemplate;

/**
 * Moves a bone from three expressions, in model units:
 * {@code {"core:translate": {"bone": "root", "y": {"mul": ["deep", 14]}}}}. On a part it writes the
 * position offset, on an entity-level vector bone ({@link Skeleton#isVectorBone}) the vector. Its
 * {@code when} is applied by the node, like every pose item's.
 */
public class TranslateDriver implements IPoseItem
{
    /** An axis an OVERRIDE write leaves out keeps the bone's current value for that axis. */
    private static final Expression UNWRITTEN = Expression.constant(Float.NaN);

    private final int slot;
    private final boolean vector;
    private final Expression x, y, z;
    private final Pose.Space space;
    private final ItemEffects effects;
    private final int[] written;

    public TranslateDriver(int slot, boolean vector, Expression x, Expression y, Expression z, Pose.Space space, ItemEffects effects)
    {
        this.slot = slot;
        this.vector = vector;
        this.x = x;
        this.y = y;
        this.z = z;
        this.space = space;
        this.effects = effects;
        this.written = new int[] { slot };
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, TranslateTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:translate needs a 'bone'.");
        }
        boolean vector = Skeleton.isVectorBone(template.bone);
        Pose.Space space = template.space == null ? Pose.Space.OVERRIDE : template.space;
        // A relative write that leaves an axis out moves it by nothing.
        Expression unwritten = space == Pose.Space.OVERRIDE ? UNWRITTEN : Expression.ZERO;
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.smoothing, template.vectorModes, template.snap, !vector);
        return new TranslateDriver(skeleton.indexOf(template.bone), vector,
                Expression.compile(template.x, context.getExpressionScope(), unwritten),
                Expression.compile(template.y, context.getExpressionScope(), unwritten),
                Expression.compile(template.z, context.getExpressionScope(), unwritten),
                space,
                effects.isEmpty() ? null : effects);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (vector)
        {
            pose.composeVector(slot, x.get(context), y.get(context), z.get(context), space);
        }
        else
        {
            pose.composeOffset(slot, x.get(context), y.get(context), z.get(context), space);
        }
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
