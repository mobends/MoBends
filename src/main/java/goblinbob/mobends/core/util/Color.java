package goblinbob.mobends.core.util;

public class Color implements IColor
{
	
	public static final ColorReadonly WHITE = new ColorReadonly(1, 1, 1, 1);
	public static final ColorReadonly RED 	= new ColorReadonly(1, 0, 0, 1);
	public static final ColorReadonly GREEN = new ColorReadonly(0, 1, 0, 1);
	public static final ColorReadonly BLUE 	= new ColorReadonly(0, 0, 1, 1);
	public static final ColorReadonly BLACK = new ColorReadonly(0, 0, 0, 1);
	public static final ColorReadonly ZERO 	= new ColorReadonly(0, 0, 0, 0);

	public float r, g, b, a;

	public Color(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0F;
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
	
	public Color(IColorRead other)
	{
		this.r = other.getR();
		this.g = other.getG();
		this.b = other.getB();
		this.a = other.getA();
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

	@Override
	public float getR()
	{
		return this.r;
	}

	@Override
	public float getG()
	{
		return this.g;
	}

	@Override
	public float getB()
	{
		return this.b;
	}

	@Override
	public float getA()
	{
		return this.a;
	}

	@Override
	public void set(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@Override
	public void setR(float r)
	{
		this.r = r;
	}

	@Override
	public void setG(float g)
	{
		this.g = g;
	}

	@Override
	public void setB(float b)
	{
		this.b = b;
	}

	@Override
	public void setA(float a)
	{
		this.a = a;
	}

	@Override
	public void add(float r, float g, float b, float a)
	{
		this.r += r;
		this.g += g;
		this.b += b;
		this.a += a;
	}
	
	public static int asHex(IColorRead color)
	{
		int valueA = (int) GUtil.clamp(color.getA() * 255F, 0, 255);
		int valueR = (int) GUtil.clamp(color.getR() * 255F, 0, 255);
		int valueG = (int) GUtil.clamp(color.getG() * 255F, 0, 255);
		int valueB = (int) GUtil.clamp(color.getB() * 255F, 0, 255);
		int value = valueA;
		value <<= 8;
		value |= valueR & 255;
		value <<= 8;
		value |= valueG & 255;
		value <<= 8;
		value |= valueB & 255;
		return value;
	}
	
	public static ColorReadonly fromHex(int hexValue)
	{
		int valueB = hexValue & 255;
		hexValue >>= 8;
		int valueG = hexValue & 255;
		hexValue >>= 8;
		int valueR = hexValue & 255;
		hexValue >>= 8;
		int valueA = hexValue & 255;
		hexValue >>= 8;
		
		return new ColorReadonly(valueR / 255.0F, valueG / 255.0F, valueB / 255.0F, valueA / 255.0F);
	}
	
}