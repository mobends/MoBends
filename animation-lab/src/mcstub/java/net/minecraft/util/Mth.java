package net.minecraft.util;

/**
 * Stand-in for the game's Mth. sin and cos use the same 65536-entry table as the game, so the
 * animation carries the same quantisation as in the game.
 */
public class Mth {
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2);
    public static final float DEG_TO_RAD = 0.017453292F;
    public static final float RAD_TO_DEG = 57.295776F;

    private static final float[] SIN = new float[65536];

    static {
        for (int i = 0; i < SIN.length; ++i) {
            SIN[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
        }
    }

    public static float sin(float value) {
        return SIN[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value) {
        return SIN[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static float wrapDegrees(float value) {
        float f = value % 360.0F;
        if (f >= 180.0F) f -= 360.0F;
        if (f < -180.0F) f += 360.0F;
        return f;
    }

    public static double wrapDegrees(double value) {
        double d = value % 360.0;
        if (d >= 180.0) d -= 360.0;
        if (d < -180.0) d += 360.0;
        return d;
    }

    public static int wrapDegrees(int value) {
        int i = value % 360;
        if (i >= 180) i -= 360;
        if (i < -180) i += 360;
        return i;
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    /** Interpolates between two angles in degrees, the short way round. */
    public static float rotLerp(float delta, float start, float end) {
        return start + delta * wrapDegrees(end - start);
    }

    /**
     * The game uses a fast table-based approximation here; the lab uses the exact one. The
     * difference is far below the parity tolerances.
     */
    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }
}
