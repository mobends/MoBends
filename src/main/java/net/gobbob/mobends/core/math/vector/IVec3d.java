package net.gobbob.mobends.core.math.vector;

public interface IVec3d extends IVec3dRead
{
	
	void set(double x, double y, double z);
	void setX(double x);
	void setY(double y);
	void setZ(double z);
	void add(double x, double y, double z);
	
	default void set(IVec3fRead vector)
	{
		this.set(vector.getX(), vector.getY(), vector.getZ());
	}
	
	default void add(IVec3fRead vector)
	{
		this.add(vector.getX(), vector.getY(), vector.getZ());
	}
	
	default void scale(double x, double y, double z)
	{
		this.set(this.getX() * x, this.getY() * y, this.getZ() * z);
	}
	
	default void scale(double a)
	{
		this.set(this.getX() * a, this.getY() * a, this.getZ() * a);
	}
	
}
