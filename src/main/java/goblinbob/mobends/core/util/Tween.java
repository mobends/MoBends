package goblinbob.mobends.core.util;

public class Tween
{
    public static double easeIn(double a, double power)
    {
        return Math.pow(a, power);
    }

    public static double easeOut(double a, double power)
    {
        return 1 - Math.pow(1 - a, power);
    }

    public static double easeInOut(double a, double power)
    {
        if (a < 0.5)
        {
            a *= 2;
            a = Math.pow(a, power);
            a /= 2;
        }
        else
        {
            a = 1 - a;
            a *= 2;
            a = Math.pow(a, power);
            a /= 2;
            a = 1 - a;
        }
        return a;
    }
}
