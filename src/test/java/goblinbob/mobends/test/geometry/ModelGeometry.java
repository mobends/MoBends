package goblinbob.mobends.test.geometry;

import net.minecraft.client.model.ModelRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** What a model draws, captured (see {@link Capture}), and how two captures differ. */
public final class ModelGeometry
{

    private static final float SCALE = 0.0625F;

    private ModelGeometry()
    {
    }

    /** The quads {@code parts} draw, rendered as a model renders them, in model units. */
    public static List<Capture.Vertex[]> render(ModelRenderer... parts)
    {
        Capture.reset();
        Capture.push();
        Capture.scale(16, 16, 16);
        for (ModelRenderer part : parts)
        {
            part.render(SCALE);
        }
        Capture.pop();
        return new ArrayList<>(Capture.drawn);
    }

    /** Where {@code part.postRender} puts what is attached to it (a held item, a hat): its matrix, in model units. */
    public static double[] postRender(ModelRenderer part)
    {
        Capture.reset();
        Capture.push();
        Capture.scale(16, 16, 16);
        part.postRender(SCALE);
        Capture.scale(1 / 16.0, 1 / 16.0, 1 / 16.0);
        double[] matrix = Capture.matrix();
        Capture.pop();
        return matrix;
    }

    /**
     * The quads of {@code actual} that match none of {@code expected} and the other way round,
     * within {@code tolerance} (model units for positions, texture fractions for coordinates).
     */
    public static List<String> differences(List<Capture.Vertex[]> expected, List<Capture.Vertex[]> actual, double tolerance)
    {
        List<Capture.Vertex[]> unmatched = new ArrayList<>(actual);
        List<String> differences = new ArrayList<>();
        for (Capture.Vertex[] quad : expected)
        {
            int match = -1;
            for (int i = 0; i < unmatched.size() && match < 0; i++)
            {
                if (same(quad, unmatched.get(i), tolerance)) match = i;
            }
            if (match < 0) differences.add("missing " + describe(quad));
            else unmatched.remove(match);
        }
        for (Capture.Vertex[] quad : unmatched)
        {
            differences.add("extra   " + describe(quad));
        }
        return differences;
    }

    /** Whether two quads have the same corners, in any rotation of their order. */
    private static boolean same(Capture.Vertex[] a, Capture.Vertex[] b, double tolerance)
    {
        for (int shift = 0; shift < 4; shift++)
        {
            boolean all = true;
            for (int k = 0; k < 4 && all; k++)
            {
                Capture.Vertex p = a[k], q = b[(k + shift) % 4];
                all = Math.abs(p.x - q.x) <= tolerance && Math.abs(p.y - q.y) <= tolerance && Math.abs(p.z - q.z) <= tolerance
                        && Math.abs(p.u - q.u) <= 1e-4 && Math.abs(p.v - q.v) <= 1e-4;
            }
            if (all) return true;
        }
        return false;
    }

    private static String describe(Capture.Vertex[] quad)
    {
        StringBuilder text = new StringBuilder();
        for (Capture.Vertex v : quad)
        {
            text.append(String.format(Locale.ROOT, "(%.3f %.3f %.3f | %.4f %.4f) ", v.x, v.y, v.z, v.u, v.v));
        }
        return text.toString();
    }

    public static String describe(double[] m)
    {
        return String.format(Locale.ROOT, "[%.3f %.3f %.3f %.3f | %.3f %.3f %.3f %.3f | %.3f %.3f %.3f %.3f]",
                m[0], m[4], m[8], m[12], m[1], m[5], m[9], m[13], m[2], m[6], m[10], m[14]);
    }

}
