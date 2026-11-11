package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;


/**
 * Evaluates the wrapped item on a mirror image of the pose and mirrors the result back; mirroring is an involution, so the item's own
 * composition semantics are untouched. The poses beneath are mirrored with it, so what the item reads of them (a weighted clip blending
 * from the layers below, an override axis left out) is on the same side. Without the layer condition it is the plain item.
 */
public class MirroredPoseItem implements IPoseItem
{

    private final IPoseItem item;
    private final LayerMirror mirror;
    private final Skeleton skeleton;
    /** False: swap the paired bones only (rotations and inputs unchanged). */
    private final boolean flip;
    /** The mirror images: the pose the item writes, then each pose beneath it, in order. */
    private final List<Pose> scratch = new ArrayList<>();

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
        int[] pairOf = mirror.pairing();
        Pose mirrored = mirrorOf(0, pose, pairOf);
        Pose above = mirrored;
        int depth = 1;
        for (Pose below = pose.getBelow(); below != null; below = below.getBelow())
        {
            Pose mirroredBelow = mirrorOf(depth++, below, pairOf);
            above.setBelow(mirroredBelow);
            above = mirroredBelow;
        }
        above.setBelow(null);
        item.apply(mirrored, context, elapsedTicks);
        pose.mirrorFrom(mirrored, pairOf, flip);
    }

    /** The scratch pose at {@code depth}, made the mirror image of {@code source}. */
    private Pose mirrorOf(int depth, Pose source, int[] pairOf)
    {
        Pose pose = depth < scratch.size() ? scratch.get(depth) : null;
        if (pose == null || pose.size() < source.size() || pose.isBase() != source.isBase())
        {
            pose = new Pose(skeleton, source.isBase());
            if (depth < scratch.size()) scratch.set(depth, pose);
            else scratch.add(pose);
        }
        pose.mirrorFrom(source, pairOf, flip);
        return pose;
    }

    @Override
    public float getDuration()
    {
        return item.getDuration();
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        item.onNodeStarted(context);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
        item.advance(context, deltaTime);
    }

}
