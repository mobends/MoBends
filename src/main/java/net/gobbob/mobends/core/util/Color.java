package net.gobbob.mobends.core.util;

public class Color
{
	public static final Color WHITE = new Color(1, 1, 1, 1);
	public static final Color RED = new Color(1, 0, 0, 1);
	public static final Color GREEN = new Color(0, 1, 0, 1);
	public static final Color BLUE = new Color(0, 0, 1, 1);
	public static final Color BLACK = new Color(0, 0, 0, 1);
	public static final Color ZERO = new Color(0, 0, 0, 0);

	public float r, g, b, a;

	public Color(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}

	public Color(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color(int hexValue)
	{
		this.hex(hexValue);
	}

	public int hex()
	{
		int valueA = (int) GUtil.clamp(this.a * 256F, 0, 255);
		int valueR = (int) GUtil.clamp(this.r * 256F, 0, 255);
		int valueG = (int) GUtil.clamp(this.g * 256F, 0, 255);
		int valueB = (int) GUtil.clamp(this.b * 256F, 0, 255);
		int value = valueA;
		value <<= 8;
		value |= valueR & 255;
		value <<= 8;
		value |= valueG & 255;
		value <<= 8;
		value |= valueB & 255;
		return value;
	}
	
	public void hex(int hexValue)
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
	
	public static Color fromHex(int hexValue)
	{
		return new Color(hexValue);
	}
}