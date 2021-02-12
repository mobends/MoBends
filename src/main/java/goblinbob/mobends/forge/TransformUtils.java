package goblinbob.mobends.forge;

import net.minecraft.util.math.vector.Quaternion;

public class TransformUtils
{
    public static void copyFromTo(Quaternion from, Quaternion to)
    {
        to.set(from.getX(), from.getY(), from.getZ(), from.getW());
    }
}
