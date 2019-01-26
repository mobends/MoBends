package net.gobbob.mobends.core.math.vector;

public interface IVec4f extends IVec4fRead
{

	void set(float x, float y, float z, float w);
	void setX(float x);
	void setY(float y);
	void setZ(float z);
	void setW(float w);
	void add(float x, float y, float z, float w);
	
	default void set(IVec4fRead vector)
	{
		this.set(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}
	
	default void add(IVec4fRead vector)
	{
		this.add(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}
	
	default void scale(float x, float y, float z, float w)
	{
		this.set(this.getX() * x, this.getY() * y, this.getZ() * z, this.getW() * w);
	}
	
	default void scale(float a)
	{
		this.set(this.getX() * a, this.getY() * a, this.getZ() * a, this.getW() * a);
	}
	
}
