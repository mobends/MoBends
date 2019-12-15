package net.gobbob.mobends.core.util;

public class ColorReadonly implements IColorRead
{

	private final float r, g, b, a;
	
	public ColorReadonly(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1F;
	}

	public ColorReadonly(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public ColorReadonly(int hexValue)
	{
		int valueB = hexValue & 255;
		hexValue >>= 8;
		int valueG = hexValue & 255;
		hexValue >>= 8;
		int valueR = hexValue & 255;
		hexValue >>= 8;
		int valueA = hexValue & 255;
		hexValue >>= 8;
		
		this.a = valueA / 255.0F;
		this.r = valueR / 255.0F;
		this.g = valueG / 255.0F;
		this.b = valueB / 255.0F;
	}
	
	@Override
	public float getR()
	{
		return r;
	}

	@Override
	public float getG()
	{
		return g;
	}

	@Override
	public float getB()
	{
		return b;
	}

	@Override
	public float getA()
	{
		return a;
	}

}
