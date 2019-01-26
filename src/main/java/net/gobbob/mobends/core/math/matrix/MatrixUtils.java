package net.gobbob.mobends.core.math.matrix;

import java.nio.FloatBuffer;

import net.gobbob.mobends.core.math.Quaternion;

public class MatrixUtils
{
	
	public static FloatBuffer matToGlMatrix(IMat4x4d matIn, FloatBuffer destBuffer)
    {
        destBuffer.clear();
        double[] fields = matIn.getFields();
        for (double f : fields)
        {
        	destBuffer.put((float) f);
        }
        destBuffer.rewind();
        return destBuffer;
    }
	
	public static void identity(IMat4x4d dest)
	{
		dest.setFields(
			1, 0, 0, 0,
			0, 1, 0, 0,
			0, 0, 1, 0,
			0, 0, 0, 1
		);
	}
	
}
