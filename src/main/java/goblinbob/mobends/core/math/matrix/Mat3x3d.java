package goblinbob.mobends.core.math.matrix;

public class Mat3x3d implements IMat3x3d
{

	public static final Mat3x3d ONE = new Mat3x3d(1);
	public static final Mat3x3d IDENTITY = new Mat3x3d(new double[] {
		1, 0, 0,
		0, 1, 0,
		0, 0, 1
	});
	
	private final double[] fields;
	
	public Mat3x3d()
	{
		this.fields = new double[9];
	}
	
	public Mat3x3d(int fillValue)
	{
		this();
		for (int i = 0; i < 9; ++i)
		{
			this.fields[i] = fillValue;
		}
	}
	
	public Mat3x3d(double[] fields)
	{
		this();
		for (int i = 0; i < this.fields.length; ++i)
		{
			this.fields[i] = fields[i];
		}
	}
	
	public Mat3x3d(IMat3x3d other)
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
		return this.fields[r + c * 3];
	}
	
	@Override
	public void set(int c, int r, double value)
	{
		this.fields[r + c * 3] = value;
	}
	
	@Override
	public void setFields(double... values)
	{
		int len = Math.min(values.length, 9);
		for (int i = 0; i < len; ++i)
		{
			this.fields[i] = values[i];
		}
	}

	@Override
	public void copyFrom(IMat3x3d src)
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
