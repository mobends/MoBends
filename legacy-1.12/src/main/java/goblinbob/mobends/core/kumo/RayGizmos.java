package goblinbob.mobends.core.kumo;

import java.util.Arrays;

/**
 * The rays {@code core:raycast} and {@code core:raycast_array} cast this frame with {@code "debug": true},
 * kept for the client to draw over the world (see {@code RayGizmoRenderer}). Nothing here draws: the
 * drivers only record, so it stays free of the renderer. Cleared as each frame's animation begins,
 * so a paused game keeps showing the last frame's rays.
 */
public final class RayGizmos
{

    /** What became of a ray. */
    public enum Kind
    {
        /** It hit nothing: drawn its whole length. */
        MISS,
        /** It hit a surface facing the wrong way ({@code core:raycast_array}'s {@code surface}): drawn up to the hit. */
        WRONG_SURFACE,
        /** It hit, but a ray closer to the middle was kept ({@code core:raycast_array}): drawn up to the hit. */
        HIT,
        /** It hit, and the hit is the driver's output: drawn up to the hit, with the surface's normal. */
        KEPT
    }

    /** The most rays kept a frame, so a frame that never ends can't grow without bound. */
    public static final int MAX_RAYS = 4096;

    /** Per ray: from x, y, z, to x, y, z, normal x, y, z. */
    private static final int STRIDE = 9;

    private static double[] rays = new double[64 * STRIDE];
    private static Kind[] kinds = new Kind[64];
    private static int count = 0;

    private RayGizmos()
    {
    }

    /** Forgets the last frame's rays. */
    public static void beginFrame()
    {
        count = 0;
    }

    public static void add(Kind kind, double fromX, double fromY, double fromZ, double toX, double toY, double toZ,
                           double normalX, double normalY, double normalZ)
    {
        if (count >= MAX_RAYS)
        {
            return;
        }
        if (count == kinds.length)
        {
            kinds = Arrays.copyOf(kinds, count * 2);
            rays = Arrays.copyOf(rays, count * 2 * STRIDE);
        }
        int i = count * STRIDE;
        rays[i] = fromX;
        rays[i + 1] = fromY;
        rays[i + 2] = fromZ;
        rays[i + 3] = toX;
        rays[i + 4] = toY;
        rays[i + 5] = toZ;
        rays[i + 6] = normalX;
        rays[i + 7] = normalY;
        rays[i + 8] = normalZ;
        kinds[count] = kind;
        count++;
    }

    public static int count()
    {
        return count;
    }

    public static Kind kind(int ray)
    {
        return kinds[ray];
    }

    /**
     * The ray's {@code component}th number: 0-2 where it starts, 3-5 where it ends (the hit, where it
     * hit), 6-8 the normal of the surface hit (zero on a miss).
     */
    public static double get(int ray, int component)
    {
        return rays[ray * STRIDE + component];
    }

}
