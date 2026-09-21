package goblinbob.mobends.core.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuts a textured box into segments along one axis so each segment can become its own bone.
 * Pure geometry: a box is {@code min}/{@code max} and six texture faces in the order
 * LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK (u, v, uSize, vSize each), the mod's box convention.
 * Side faces along the axis get the matching strip of their texture; the end caps stay on the
 * first and the last segment only.
 */
public final class BoxSplitter
{

    public static final int LEFT = 0, RIGHT = 1, TOP = 2, BOTTOM = 3, FRONT = 4, BACK = 5;

    /** Faces at the low / high end of each axis, and the faces that run along it with the UV coordinate (0 = u, 1 = v) that follows it. */
    private static final int[][] CAPS = { { RIGHT, LEFT }, { TOP, BOTTOM }, { FRONT, BACK } };
    private static final int[][] STRIPS = { { TOP, BOTTOM, FRONT, BACK }, { LEFT, RIGHT, FRONT, BACK }, { LEFT, RIGHT, TOP, BOTTOM } };
    private static final int[][] STRIP_COORD = { { 0, 0, 0, 0 }, { 1, 1, 1, 1 }, { 0, 0, 1, 1 } };

    private BoxSplitter()
    {
    }

    public static class Segment
    {
        /** Box corners in the segment's own frame (its pivot is the origin). */
        public final float[] min = new float[3];
        public final float[] max = new float[3];
        /** The pivot relative to the previous segment's origin (the bone origin for the first). */
        public final float[] pivot = new float[3];
        /** Six faces as {u, v, uSize, vSize}. */
        public final float[][] faces = new float[6][4];
        /** Bit i set = face i visible. */
        public int visibility = 0b111111;
    }

    /**
     * @param min       box corner (bone frame)
     * @param max       opposite corner
     * @param faces     six texture faces {u, v, uSize, vSize}
     * @param axis      0 = X, 1 = Y, 2 = Z
     * @param fractions cut positions as fractions of the box length along the axis, increasing in (0, 1)
     */
    public static List<Segment> split(float[] min, float[] max, float[][] faces, int axis, float[] fractions)
    {
        float length = max[axis] - min[axis];
        int count = fractions.length + 1;
        float[] cuts = new float[count + 1];
        cuts[0] = min[axis];
        for (int i = 0; i < fractions.length; i++)
        {
            cuts[i + 1] = min[axis] + length * fractions[i];
        }
        cuts[count] = max[axis];

        List<Segment> segments = new ArrayList<>();
        float previousOrigin = 0; // along the axis, in the bone frame
        for (int k = 0; k < count; k++)
        {
            Segment segment = new Segment();
            float origin = k == 0 ? 0 : cuts[k];
            for (int i = 0; i < 3; i++)
            {
                segment.min[i] = min[i];
                segment.max[i] = max[i];
                segment.pivot[i] = 0;
            }
            segment.min[axis] = cuts[k] - origin;
            segment.max[axis] = cuts[k + 1] - origin;
            segment.pivot[axis] = origin - previousOrigin;
            previousOrigin = origin;

            for (int f = 0; f < 6; f++)
            {
                System.arraycopy(faces[f], 0, segment.faces[f], 0, 4);
            }
            // The strips along the axis get their share of the texture, proportionally.
            float from = (cuts[k] - min[axis]) / length;
            float to = (cuts[k + 1] - min[axis]) / length;
            for (int s = 0; s < 4; s++)
            {
                int face = STRIPS[axis][s];
                int coord = STRIP_COORD[axis][s];
                float start = faces[face][coord];
                float size = faces[face][coord + 2];
                segment.faces[face][coord] = start + size * from;
                segment.faces[face][coord + 2] = size * (to - from);
            }
            // End caps only on the outer segments.
            if (k > 0) segment.visibility &= ~(1 << CAPS[axis][0]);
            if (k < count - 1) segment.visibility &= ~(1 << CAPS[axis][1]);
            segments.add(segment);
        }
        return segments;
    }

}
