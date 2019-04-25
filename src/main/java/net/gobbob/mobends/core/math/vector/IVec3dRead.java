package net.gobbob.mobends.core.math.vector;

public interface IVec3dRead
{

    double getX();

    double getY();

    double getZ();

    double lengthSq();

    default double length()
    {
        return Math.sqrt(this.lengthSq());
    }

}
