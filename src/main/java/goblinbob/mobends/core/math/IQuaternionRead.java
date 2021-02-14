package goblinbob.mobends.core.math;

public interface IQuaternionRead
{
    float getX();

    float getY();

    float getZ();

    float getW();

    default float lengthSquared()
    {
        return getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW();
    }

    default float length()
    {
        return (float) Math.sqrt(this.lengthSquared());
    }
}
