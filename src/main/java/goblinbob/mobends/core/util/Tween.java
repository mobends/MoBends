package goblinbob.mobends.core.util;

public class Tween
{

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
