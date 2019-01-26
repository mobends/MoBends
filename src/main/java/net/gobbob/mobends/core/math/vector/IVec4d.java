package net.gobbob.mobends.core.math.vector;

public interface IVec4d extends IVec4dRead
{

	void set(double x, double y, double z, double w);
	void setX(double x);
	void setY(double y);
	void setZ(double z);
	void setW(double w);
	void add(double x, double y, double z, double w);
	
	default void set(IVec4dRead vector)
	{
		this.set(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}
	
	default void add(IVec4dRead vector)
	{
		this.add(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
	}
	
	default void scale(double x, double y, double z, double w)
	{
		this.set(this.getX() * x, this.getY() * y, this.getZ() * z, this.getW() * w);
	}
	
	default void scale(double a)
	{
		this.set(this.getX() * a, this.getY() * a, this.getZ() * a, this.getW() * a);
	}
	
}
