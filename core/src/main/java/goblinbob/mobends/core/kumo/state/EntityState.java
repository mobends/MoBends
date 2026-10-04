package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.pose.Pose;

/**
 * One entity's state of an animator (see {@link StateLayout}): its slots' values and its pose
 * buffers. The compiled animator, shared by every entity it was compiled for, reads and writes it
 * through the context.
 */
public final class EntityState
{

    public float[] floats = new float[0];
    public int[] ints = new int[0];
    public Pose[] poses = new Pose[0];

    EntityState()
    {
    }

}
