package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.pose.*;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AxisRotateTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.util.EnumAxis;

/**
 * Rotates one bone about a fixed axis by an angle taken from a {@link Expression}
 * (in degrees), composed in PRE or POST space: head-look, body-twist, compensating for a
 * parent's rotation. Its {@code when} is applied by the node, like every pose item's.
 */
public class AxisRotateDriver implements IPoseItem
{

    private final int slot;
    private final EnumAxis axis;
    private final Expression angle;
    private final Pose.Space space;
    private final ItemEffects effects;
    private final int[] written;
    private final Quaternion rotation = new Quaternion();

    public AxisRotateDriver(int slot, EnumAxis axis, Expression angle, Pose.Space space, ItemEffects effects)
    {
        this.slot = slot;
        this.axis = axis;
        this.angle = angle;
        this.space = space;
        this.effects = effects;
        this.written = new int[] { slot };
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, AxisRotateTemplate template) throws MalformedKumoTemplateException
    {
        if (template.bone == null)
        {
            throw new MalformedKumoTemplateException("core:axis_rotate needs a 'bone'.");
        }
        if (template.axis == null)
        {
            throw new MalformedKumoTemplateException("core:axis_rotate needs an 'axis' (X, Y or Z).");
        }
        Expression angle = Expression.compile(template.angle, context.getExpressionScope(), Expression.ZERO);
        Pose.Space space = template.space == null ? Pose.Space.PRE : template.space;
        ItemEffects effects = new ItemEffects(context.getExpressionScope(), skeleton, template.damping, template.vectorModes, template.snap);
        return new AxisRotateDriver(skeleton.indexOf(template.bone), template.axis, angle, space, effects.isEmpty() ? null : effects);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        float degrees = angle.get(context);
        PoseMath.axisAngleDegrees(axis == EnumAxis.X ? 1 : 0, axis == EnumAxis.Y ? 1 : 0, axis == EnumAxis.Z ? 1 : 0, degrees, rotation);
        pose.composeRotation(slot, rotation, space);
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
