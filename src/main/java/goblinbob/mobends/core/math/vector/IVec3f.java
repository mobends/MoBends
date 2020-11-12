package goblinbob.mobends.core.math.vector;

public interface IVec3f extends IVec3fRead
{

    void set(float x, float y, float z);

    void setX(float x);

    void setY(float y);

    void setZ(float z);

    void add(float x, float y, float z);

    default void set(IVec3fRead vector)
    {
        this.set(vector.getX(), vector.getY(), vector.getZ());
    }

    default void add(IVec3fRead vector)
    {
        this.add(vector.getX(), vector.getY(), vector.getZ());
    }

    default void scale(float x, float y, float z)
    {
        this.set(this.getX() * x, this.getY() * y, this.getZ() * z);
    }

    default void scale(float a)
    {
        this.set(this.getX() * a, this.getY() * a, this.getZ() * a);
    }

}
