package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import javax.annotation.Nullable;

/**
 * The first item of a {@code core:vanilla_pose} node: writes each bone the subject's vanilla pose
 * has ({@link IVanillaPose}) as that pose has it, its rotation, offset and scale replacing what
 * lies beneath. A bone it has no pose for is left alone. The entity's own bones ({@code root},
 * {@code center}), once any bone has a pose, are given the entity's frame as vanilla draws it:
 * as the vanilla pose has them (the host gives vanilla's turns of the whole entity as the root's
 * pose), else at rest, as vanilla's parts are drawn in the entity's frame. They're then blended
 * like any bone, and the entity's frame is vanilla's (the pose's {@link Pose#setVanillaFrame},
 * see {@link goblinbob.mobends.core.kumo.IKumoSubject#setEntityFrameVanilla}).
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
    private final Vec3f scale = new Vec3f();

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
            scale.set(1, 1, 1);
            if (skeleton.isEntityBone(slot) || !vanilla.read(skeleton.nameOf(slot), rotation, offset, scale))
            {
                continue;
            }
            pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
            pose.composeOffset(slot, offset.x, offset.y, offset.z, Pose.Space.OVERRIDE);
            pose.composeScale(slot, scale.x, scale.y, scale.z, Pose.Space.OVERRIDE);
            read = true;
        }
        if (!read)
        {
            return;
        }
        // Vanilla's parts are drawn in the entity's frame: nothing of the animator's own moves it,
        // only what vanilla turns the whole entity by.
        boolean framed = false;
        for (int i = 0; i < count; i++)
        {
            int slot = bones != null ? bones[i] : i;
            if (skeleton.isEntityBone(slot))
            {
                if (!vanilla.read(skeleton.nameOf(slot), rotation, offset, scale))
                {
                    rotation.setIdentity();
                    offset.set(0, 0, 0);
                }
                pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
                pose.composeVector(slot, offset.x, offset.y, offset.z, Pose.Space.OVERRIDE);
                framed = true;
            }
        }
        if (framed)
        {
            pose.setVanillaFrame(1F);
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
