package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import javax.annotation.Nullable;

/**
 * The first item of a {@code core:vanilla_pose} node: writes each bone the subject's vanilla pose
 * has ({@link IVanillaPose}) as that pose has it, its rotation and offset replacing what lies
 * beneath. A bone it has no pose for is left alone. The entity's own bones ({@code root},
 * {@code center}), which the vanilla model draws nothing of (its parts are drawn in the entity's
 * frame), are put at rest, once any bone has a pose, and the entity's frame is left to vanilla
 * ({@link IVanillaPose#onEntityFrameLeftToVanilla}).
 */
public class VanillaPoseItem implements IPoseItem
{

    private final Skeleton skeleton;
    /** The bones it poses, by index; null for every bone of the skeleton. */
    @Nullable
    private final int[] bones;
    // Scratch, used within one apply (the render thread's).
    private final Quaternion rotation = new Quaternion();
    private final Vec3f offset = new Vec3f();

    public VanillaPoseItem(Skeleton skeleton, @Nullable int[] bones)
    {
        this.skeleton = skeleton;
        this.bones = bones;
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks)
    {
        IVanillaPose vanilla = context.getSubject().getVanillaPose();
        if (vanilla == null)
        {
            return;
        }
        // Every bone of the skeleton is known by now: the program was compiled before it ran.
        int count = bones != null ? bones.length : skeleton.size();
        boolean read = false;
        for (int i = 0; i < count; i++)
        {
            int slot = bones != null ? bones[i] : i;
            if (skeleton.isEntityBone(slot) || !vanilla.read(skeleton.nameOf(slot), rotation, offset))
            {
                continue;
            }
            pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
            pose.composeOffset(slot, offset.x, offset.y, offset.z, Pose.Space.OVERRIDE);
            read = true;
        }
        if (!read)
        {
            return;
        }
        // Vanilla's parts are drawn in the entity's frame: nothing of the animator's own moves it.
        rotation.setIdentity();
        boolean atRest = false;
        for (int i = 0; i < count; i++)
        {
            int slot = bones != null ? bones[i] : i;
            if (skeleton.isEntityBone(slot))
            {
                pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
                pose.composeVector(slot, 0, 0, 0, Pose.Space.OVERRIDE);
                atRest = true;
            }
        }
        if (atRest)
        {
            vanilla.onEntityFrameLeftToVanilla();
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
