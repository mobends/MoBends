package goblinbob.mobends.standard.client.model.armor;

import java.util.Arrays;

/**
 * Quads to draw in one frame of an armour wrapper's part, built anew every frame: armour that isn't
 * made of vanilla boxes (GeckoLib's) is posed by drawing its faces in the parts' frames, a limb's
 * cut at its joint ({@link #slice}) as the wrapper cuts vanilla boxes.
 * <p>
 * A vertex is {@link #STRIDE} floats: its position, texture coordinates and normal.
 */
public class ArmorMesh
{
    public static final int STRIDE = 8;

    private float[] data = new float[STRIDE * 64];
    private int vertexCount = 0;

    public void clear()
    {
        vertexCount = 0;
    }

    public boolean isEmpty()
    {
        return vertexCount == 0;
    }

    /** The vertices, four to a quad; the first {@link #getVertexCount} of them are this mesh's. */
    public float[] getData()
    {
        return data;
    }

    public int getVertexCount()
    {
        return vertexCount;
    }

    /**
     * Adds a convex polygon of {@code count} vertices ({@link #STRIDE} floats each, from
     * {@code polygon[start]}), as quads: a fan from its first vertex, the last quad of an odd fan
     * a triangle with its last vertex twice.
     */
    public void addPolygon(float[] polygon, int start, int count)
    {
        for (int i = 1; i + 1 < count; i += 2)
        {
            addVertex(polygon, start);
            addVertex(polygon, start + i * STRIDE);
            addVertex(polygon, start + (i + 1) * STRIDE);
            addVertex(polygon, start + Math.min(i + 2, count - 1) * STRIDE);
        }
    }

    private void addVertex(float[] polygon, int at)
    {
        if ((vertexCount + 1) * STRIDE > data.length)
        {
            data = Arrays.copyOf(data, data.length * 2);
        }
        System.arraycopy(polygon, at, data, vertexCount * STRIDE, STRIDE);
        vertexCount++;
    }

    /**
     * Cuts a convex polygon along {@code y = cut}: what is above it (lesser y, as a vanilla part
     * hangs down from its pivot) goes to {@code above}, the rest to {@code below}, and a polygon
     * lying on the cut to {@code above}. The new edge is left open, as the wrapper leaves a cut
     * box's cut faces out; texture coordinates are interpolated.
     *
     * @param scratch room for two polygons of {@code count + 1} vertices, see {@link #scratchFor}.
     */
    public static void slice(float[] polygon, int count, float cut, ArmorMesh above, ArmorMesh below, float[] scratch)
    {
        final int half = (count + 1) * STRIDE;
        int aboveCount = 0, belowCount = 0;
        boolean reachesAbove = false, reachesBelow = false;

        for (int i = 0; i < count; i++)
        {
            final int a = i * STRIDE, b = ((i + 1) % count) * STRIDE;
            final float ya = polygon[a + 1], yb = polygon[b + 1];
            reachesAbove |= ya < cut;
            reachesBelow |= ya > cut;

            if (ya <= cut)
            {
                System.arraycopy(polygon, a, scratch, aboveCount++ * STRIDE, STRIDE);
            }
            if (ya >= cut)
            {
                System.arraycopy(polygon, a, scratch, half + belowCount++ * STRIDE, STRIDE);
            }
            if ((ya < cut && yb > cut) || (ya > cut && yb < cut))
            {
                final float t = (cut - ya) / (yb - ya);
                final int onAbove = aboveCount++ * STRIDE, onBelow = half + belowCount++ * STRIDE;
                for (int k = 0; k < STRIDE; k++)
                {
                    scratch[onAbove + k] = polygon[a + k] + (polygon[b + k] - polygon[a + k]) * t;
                }
                scratch[onAbove + 1] = cut;
                System.arraycopy(scratch, onAbove, scratch, onBelow, STRIDE);
            }
        }

        if (reachesAbove || !reachesBelow)
        {
            above.addPolygon(scratch, 0, aboveCount);
        }
        if (reachesBelow)
        {
            below.addPolygon(scratch, half, belowCount);
        }
    }

    /** A scratch array {@link #slice} can cut polygons of up to {@code count} vertices with. */
    public static float[] scratchFor(int count)
    {
        return new float[2 * (count + 1) * STRIDE];
    }
}
