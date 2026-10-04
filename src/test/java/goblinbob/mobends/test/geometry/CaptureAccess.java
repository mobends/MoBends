package goblinbob.mobends.test.geometry;

/** The capture's recording side, for the stand-ins in Minecraft's packages. */
public final class CaptureAccess
{
    private CaptureAccess() { }
    public static void push() { Capture.push(); }
    public static void pop() { Capture.pop(); }
    public static void translate(double x, double y, double z) { Capture.translate(x, y, z); }
    public static void scale(double x, double y, double z) { Capture.scale(x, y, z); }
    public static void rotate(double angle, double x, double y, double z) { Capture.rotate(angle, x, y, z); }
    public static void multiply(double[] m) { Capture.multiply(m); }
    public static int newLists(int count) { return Capture.newLists(count); }
    public static void beginList(int list) { Capture.beginList(list); }
    public static void endList() { Capture.endList(); }
    public static void pos(double x, double y, double z) { Capture.pos(x, y, z); }
    public static void tex(double u, double v) { Capture.tex(u, v); }
    public static void endVertex() { Capture.endVertex(); }
    public static void draw() { Capture.draw(); }
    public static void callList(int list) { Capture.callList(list); }
}
