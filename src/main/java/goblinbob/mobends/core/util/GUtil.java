package goblinbob.mobends.core.util;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.QuaternionUtils;
import goblinbob.mobends.core.math.vector.IVec3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GUtil
{

    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) Math.PI * 2;
    public static final float RAD_TO_DEG = 180.0F / PI;
    public static final float DEG_TO_RAD = PI / 180.0F;

    public static float clamp(float value, float min, float max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static int clamp(int value, int min, int max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static double angleFromCoordinates(double x, double z)
    {
        return Math.atan2(x, z) / Math.PI * 180.0;
    }

    public static double getRadianDifference(double a, double b)
    {
        a = wrapRadians(a);
        b = wrapRadians(b);
        double d = Math.abs(a - b);
        d = d > Math.PI ? Math.PI * 2 - d : d;
        return d;
    }

    public static double wrapRadians(double a)
    {
        a = a % Math.PI;

        if (a >= Math.PI)
        {
            a -= Math.PI * 2;
        }
        else if (a < -Math.PI)
        {
            a += Math.PI * 2;
        }

        return a;
    }

    public static IVec3f translate(IVec3f vector, float x, float y, float z)
    {
        vector.add(x, y, z);
        return vector;
    }

    public static IVec3f scale(IVec3f vector, float x, float y, float z)
    {
        vector.scale(x, y, z);
        return vector;
    }

    public static void rotate(IVec3f[] points, Quaternion rotation)
    {
        for (IVec3f point : points)
        {
            QuaternionUtils.multiply(point, rotation, point);
        }
    }

    public static float lerp(float a, float b, float slide)
    {
        return a + (b - a) * slide;
    }

    public static float interpolateRotation(float a, float b, float partialTicks)
    {
        float f;
        for (f = b - a; f < -180.0F; f += 360.0F) ;

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return a + partialTicks * f;
    }

    public static float interpolateRadians(float a, float b, float partialTicks)
    {
        float f = b - a;

        while (f < -PI)
            f += TWO_PI;

        while (f >= PI)
            f -= TWO_PI;

        return a + partialTicks * f;
    }

    public static IVec3f[] translate(IVec3f[] vectors, float x, float y, float z)
    {
        for (IVec3f vector : vectors)
        {
            translate(vector, x, y, z);
        }
        return vectors;
    }

    public static IVec3f[] scale(IVec3f[] vectors, float x, float y, float z)
    {
        for (IVec3f vector : vectors)
        {
            scale(vector, x, y, z);
        }
        return vectors;
    }

    public static String[] readLines(BufferedReader reader)
    {
        try
        {
            List<String> lines = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null)
            {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
            return lines.toArray(new String[0]);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] readLines(File file)
    {
        try
        {
            return readLines(new BufferedReader(new FileReader(file)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(BufferedReader reader)
    {
        try
        {
            String content = "";
            String line;
            while ((line = reader.readLine()) != null)
            {
                content += line + "\n";
            }
            reader.close();
            return content;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(File file)
    {
        try
        {
            return readFile(new BufferedReader(new FileReader(file)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeLines(File file, String[] lines)
    {
        BufferedWriter os;
        try
        {
            os = new BufferedWriter(new FileWriter(file));
            for (String line : lines)
            {
                os.write(line);
                os.newLine();
            }
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
