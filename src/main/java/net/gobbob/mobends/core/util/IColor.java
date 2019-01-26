package net.gobbob.mobends.core.util;

public interface IColor extends IColorRead
{
	
	void setR(float r);
	void setG(float g);
	void setB(float b);
	void setA(float a);
	void set(float r, float g, float b, float a);
	void add(float r, float g, float b, float a);
	
}
