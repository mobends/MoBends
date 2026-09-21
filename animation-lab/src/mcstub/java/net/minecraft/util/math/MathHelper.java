package net.minecraft.util.math;

/**
 * Stub of Minecraft 1.12.2's MathHelper. sin/cos use the same 65536-entry lookup table as the game
 * so the reference animation output carries the same quantisation.
 */
public class MathHelper
{
    private static final float[] SIN_TABLE = new float[65536];

    static
    {
        for (int i = 0; i < 65536; ++i)
        {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }
    }

    public static float sin(float value)
    {
        return SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value)
    {
        return SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt(float value) { return (float) Math.sqrt((double) value); }
    public static float sqrt(double value) { return (float) Math.sqrt(value); }

    public static int floor(float value)
    {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floor(double value)
    {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static float abs(float value) { return value >= 0.0F ? value : -value; }
    public static int abs(int value) { return value >= 0 ? value : -value; }

    public static int clamp(int num, int min, int max) { return num < min ? min : (num > max ? max : num); }
    public static float clamp(float num, float min, float max) { return num < min ? min : (num > max ? max : num); }
    public static double clamp(double num, double min, double max) { return num < min ? min : (num > max ? max : num); }

    public static float wrapDegrees(float value)
    {
        value = value % 360.0F;
        if (value >= 180.0F) value -= 360.0F;
        if (value < -180.0F) value += 360.0F;
        return value;
    }

    public static double wrapDegrees(double value)
    {
        value = value % 360.0D;
        if (value >= 180.0D) value -= 360.0D;
        if (value < -180.0D) value += 360.0D;
        return value;
    }

    public static int wrapDegrees(int angle)
    {
        angle = angle % 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    /**
     * The game uses a fast table-based approximation here. The exact version is used in the lab;
     * the difference is far below the parity tolerances and only affects FlyingAnimationBit.
     */
    public static double atan2(double y, double x) { return Math.atan2(y, x); }

    public static double atan(double value) { return Math.atan(value); }
}
