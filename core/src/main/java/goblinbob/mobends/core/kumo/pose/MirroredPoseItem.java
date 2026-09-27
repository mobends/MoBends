package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Set;

/**
 * Evaluates the wrapped item on a mirror image of the pose, with the layer's yaw-like variables
 * negated, and mirrors the result back; mirroring is an involution, so the item's own
 * composition semantics are untouched. Without the layer condition it is the plain item.
 */
public class MirroredPoseItem implements IPoseItem
{

    private final IPoseItem item;
    private final LayerMirror mirror;
    private final Skeleton skeleton;
    /** False: swap the paired bones only (rotations and inputs unchanged). */
    private final boolean flip;
    private Pose scratch;

    public MirroredPoseItem(IPoseItem item, LayerMirror mirror, Skeleton skeleton, boolean flip)
    {
        this.item = item;
        this.mirror = mirror;
        this.skeleton = skeleton;
        this.flip = flip;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (!mirror.isActive(context))
        {
            item.apply(pose, context, elapsedTicks);
            return;
        }
        if (scratch == null || scratch.size() < pose.size())
        {
            scratch = new Pose(skeleton);
        }
        int[] pairOf = mirror.pairing();
        scratch.mirrorFrom(pose, pairOf, flip);
        if (!flip)
        {
            item.apply(scratch, context, elapsedTicks);
        }
        else
        {
            Set<String> previous = context.setNegatedVariables(mirror.getNegatedVariables());
            try
            {
                item.apply(scratch, context, elapsedTicks);
            }
            finally
            {
                context.setNegatedVariables(previous);
            }
        }
        pose.mirrorFrom(scratch, pairOf, flip);
    }

    @Override
    public float getDuration()
    {
        return item.getDuration();
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        mirror.onNodeStarted(context);
        item.onNodeStarted(context);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        item.advance(context, deltaTime);
    }

}
