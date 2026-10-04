package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;

import java.util.Arrays;

/**
 * Where an animator's per-entity state goes, decided while it is compiled: every stateful element
 * (a node's clock, a layer's current node, an edge trigger's memory, a definition's value, a
 * driver's declared state) takes slots here, and the compiled animator holds only their indices.
 * An entity's {@link EntityState} has the slots' values.
 */
public final class StateLayout
{

    private float[] floatInitials = new float[16];
    private int floatCount;
    private int[] intInitials = new int[16];
    private int intCount;
    private boolean[] poseSinkFallback = new boolean[4];
    private int poseCount;

    /** {@code count} float slots, each {@code initial} in a new entity's state; the first one's index. */
    public int floats(int count, float initial)
    {
        if (floatCount + count > floatInitials.length)
        {
            floatInitials = Arrays.copyOf(floatInitials, Math.max(floatInitials.length * 2, floatCount + count));
        }
        Arrays.fill(floatInitials, floatCount, floatCount + count, initial);
        int first = floatCount;
        floatCount += count;
        return first;
    }

    /** {@code count} int slots (indices, counters a float would round), each {@code initial}; the first one's index. */
    public int ints(int count, int initial)
    {
        if (intCount + count > intInitials.length)
        {
            intInitials = Arrays.copyOf(intInitials, Math.max(intInitials.length * 2, intCount + count));
        }
        Arrays.fill(intInitials, intCount, intCount + count, initial);
        int first = intCount;
        intCount += count;
        return first;
    }

    /** A pose buffer each entity has of its own; its index. */
    public int pose()
    {
        return pose(false);
    }

    /** A pose buffer; {@code sinkFallback}: see {@link Pose#Pose(Skeleton, boolean)}. */
    public int pose(boolean sinkFallback)
    {
        if (poseCount == poseSinkFallback.length)
        {
            poseSinkFallback = Arrays.copyOf(poseSinkFallback, poseCount * 2);
        }
        poseSinkFallback[poseCount] = sinkFallback;
        return poseCount++;
    }

    /** A new entity's state: every slot at its initial value, its pose buffers sized for {@code skeleton}. */
    public EntityState newState(Skeleton skeleton)
    {
        EntityState state = new EntityState();
        grow(state, skeleton);
        return state;
    }

    /** Adds to {@code state} the slots taken since it was made, at their initial values. */
    public void grow(EntityState state, Skeleton skeleton)
    {
        if (state.floats.length < floatCount)
        {
            int from = state.floats.length;
            state.floats = Arrays.copyOf(state.floats, floatCount);
            System.arraycopy(floatInitials, from, state.floats, from, floatCount - from);
        }
        if (state.ints.length < intCount)
        {
            int from = state.ints.length;
            state.ints = Arrays.copyOf(state.ints, intCount);
            System.arraycopy(intInitials, from, state.ints, from, intCount - from);
        }
        if (state.poses.length < poseCount)
        {
            int from = state.poses.length;
            state.poses = Arrays.copyOf(state.poses, poseCount);
            for (int i = from; i < poseCount; i++)
            {
                state.poses[i] = new Pose(skeleton, poseSinkFallback[i], state);
            }
        }
    }

    public int floatCount()
    {
        return floatCount;
    }

}
