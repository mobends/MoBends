package goblinbob.mobends.test.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What the test stand-ins for Minecraft's GL classes record: the matrix stack, the vertices each
 * display list holds, and every vertex drawn, in model space. A model can then be rendered with no
 * GL context, and what it would draw compared.
 */
public final class Capture
{

    /** A vertex as drawn: its position after the matrix it was drawn with, and its texture coordinates. */
    public static final class Vertex
    {
        public final double x, y, z, u, v;

        Vertex(double x, double y, double z, double u, double v)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString()
        {
            return String.format("(%.3f, %.3f, %.3f | %.4f, %.4f)", x, y, z, u, v);
        }
    }

    private static final List<double[]> stack = new ArrayList<>();
    private static double[] matrix = identity();
    private static final Map<Integer, List<double[]>> lists = new HashMap<>();
    private static List<double[]> compiling;
    private static final List<double[]> pending = new ArrayList<>();
    private static double[] vertex = new double[5];
    private static int nextList = 1;
    /** Every quad drawn since the last {@link #reset}, four vertices each. */
    public static final List<Vertex[]> drawn = new ArrayList<>();

    private Capture()
    {
    }

    public static void reset()
    {
        stack.clear();
        matrix = identity();
        drawn.clear();
    }

    /** The current matrix (column-major, as GL keeps it). */
    public static double[] matrix()
    {
        return matrix.clone();
    }

    static double[] identity()
    {
        return new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
    }

    static void push()
    {
        stack.add(matrix.clone());
    }

    static void pop()
    {
        matrix = stack.remove(stack.size() - 1);
    }

    static void multiply(double[] m)
    {
        double[] r = new double[16];
        for (int c = 0; c < 4; c++)
        {
            for (int row = 0; row < 4; row++)
            {
                double sum = 0;
                for (int k = 0; k < 4; k++)
                {
                    sum += matrix[k * 4 + row] * m[c * 4 + k];
                }
                r[c * 4 + row] = sum;
            }
        }
        matrix = r;
    }

    static void translate(double x, double y, double z)
    {
        double[] m = identity();
        m[12] = x;
        m[13] = y;
        m[14] = z;
        multiply(m);
    }

    static void scale(double x, double y, double z)
    {
        double[] m = identity();
        m[0] = x;
        m[5] = y;
        m[10] = z;
        multiply(m);
    }

    static void rotate(double degrees, double x, double y, double z)
    {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0) return;
        x /= length;
        y /= length;
        z /= length;
        double a = Math.toRadians(degrees), c = Math.cos(a), s = Math.sin(a), t = 1 - c;
        multiply(new double[] {
                t * x * x + c, t * x * y + s * z, t * x * z - s * y, 0,
                t * x * y - s * z, t * y * y + c, t * y * z + s * x, 0,
                t * x * z + s * y, t * y * z - s * x, t * z * z + c, 0,
                0, 0, 0, 1 });
    }

    static int newLists(int count)
    {
        int first = nextList;
        nextList += count;
        return first;
    }

    static void beginList(int list)
    {
        compiling = new ArrayList<>();
        lists.put(list, compiling);
    }

    static void endList()
    {
        compiling = null;
    }

    static void pos(double x, double y, double z)
    {
        vertex[0] = x;
        vertex[1] = y;
        vertex[2] = z;
    }

    static void tex(double u, double v)
    {
        vertex[3] = u;
        vertex[4] = v;
    }

    static void endVertex()
    {
        pending.add(vertex.clone());
    }

    /** The tessellator drew: into the list being compiled, or straight to the screen. */
    static void draw()
    {
        if (compiling != null)
        {
            compiling.addAll(pending);
        }
        else
        {
            emit(pending);
        }
        pending.clear();
    }

    static void callList(int list)
    {
        List<double[]> vertices = lists.get(list);
        if (vertices != null)
        {
            emit(vertices);
        }
    }

    private static void emit(List<double[]> vertices)
    {
        for (int i = 0; i + 3 < vertices.size(); i += 4)
        {
            Vertex[] quad = new Vertex[4];
            for (int k = 0; k < 4; k++)
            {
                double[] p = vertices.get(i + k);
                quad[k] = new Vertex(
                        matrix[0] * p[0] + matrix[4] * p[1] + matrix[8] * p[2] + matrix[12],
                        matrix[1] * p[0] + matrix[5] * p[1] + matrix[9] * p[2] + matrix[13],
                        matrix[2] * p[0] + matrix[6] * p[1] + matrix[10] * p[2] + matrix[14],
                        p[3], p[4]);
            }
            drawn.add(quad);
        }
    }

}
