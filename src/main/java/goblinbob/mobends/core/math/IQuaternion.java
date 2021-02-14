package goblinbob.mobends.core.math;

public interface IQuaternion extends IQuaternionRead
{
    void setX(float x);
    void setY(float y);
    void setZ(float z);
    void setW(float w);
    void set(IQuaternionRead src);
    void set(float x, float y, float z, float w);
    void add(float x, float y, float z, float w);
}
