package goblinbob.mobends.core.math.matrix;

public class Mat4x4d implements IMat4x4d
{

	public static final Mat4x4d ONE = new Mat4x4d(1);
	public static final Mat4x4d IDENTITY = new Mat4x4d(new double[] {
		1, 0, 0, 0,
		0, 1, 0, 0,
		0, 0, 1, 0,
		0, 0, 0, 1
	});
	
	private final double[] fields;
	
	public Mat4x4d()
	{
		this.fields = new double[16];
	}
	
	public Mat4x4d(int fillValue)
	{
		this();
		for (int i = 0; i < 16; ++i)
		{
			this.fields[i] = fillValue;
		}
	}
	
	public Mat4x4d(double[] fields)
	{
		this();
		for (int i = 0; i < this.fields.length; ++i)
		{
			this.fields[i] = fields[i];
		}
	}
	
	public Mat4x4d(IMat4x4d other)
	{
		this();
		double[] otherFields = other.getFields();
		for (int i = 0; i < this.fields.length; ++i)
		{
			this.fields[i] = otherFields[i];
		}
	}
	
	@Override
	public double[] getFields()
	{
		return this.fields;
	}

	@Override
	public double get(int c, int r)
	{
		return this.fields[r + c * 4];
	}
	
	@Override
	public void set(int c, int r, double value)
	{
		this.fields[r + c * 4] = value;
	}
	
	@Override
	public void setFields(double... values)
	{
		int len = Math.min(values.length, 16);
		for (int i = 0; i < len; ++i)
		{
			this.fields[i] = values[i];
		}
	}

	@Override
	public void copyFrom(IMat4x4d src)
	{
		double[] srcFields = src.getFields();
		for (int i = 0; i < this.fields.length; ++i)
		{
			this.fields[i] = srcFields[i];
		}
	}

	@Override
	public void scale(double scalar)
	{
		for (int i = 0; i < this.fields.length; ++i)
		{
			this.fields[i] *= scalar;
		}
	}

}
