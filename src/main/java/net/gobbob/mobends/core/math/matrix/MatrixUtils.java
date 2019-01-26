package net.gobbob.mobends.core.math.matrix;

import java.nio.FloatBuffer;

import net.gobbob.mobends.core.math.Quaternion;
import net.gobbob.mobends.core.math.vector.Vec4d;

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
	
	public static void inverse(IMat4x4d src, IMat4x4d dest)
	{
		double[] f = src.getFields();
		
		double Coef00 = f[2*4 + 2] * f[3*4 + 3] - f[3*4 + 2] * f[2*4 + 3];
		double Coef02 = f[1*4 + 2] * f[3*4 + 3] - f[3*4 + 2] * f[1*4 + 3];
		double Coef03 = f[1*4 + 2] * f[2*4 + 3] - f[2*4 + 2] * f[1*4 + 3];

		double Coef04 = f[2*4 + 1] * f[3*4 + 3] - f[3*4 + 1] * f[2*4 + 3];
		double Coef06 = f[1*4 + 1] * f[3*4 + 3] - f[3*4 + 1] * f[1*4 + 3];
		double Coef07 = f[1*4 + 1] * f[2*4 + 3] - f[2*4 + 1] * f[1*4 + 3];

		double Coef08 = f[2*4 + 1] * f[3*4 + 2] - f[3*4 + 1] * f[2*4 + 2];
		double Coef10 = f[1*4 + 1] * f[3*4 + 2] - f[3*4 + 1] * f[1*4 + 2];
		double Coef11 = f[1*4 + 1] * f[2*4 + 2] - f[2*4 + 1] * f[1*4 + 2];

		double Coef12 = f[2*4 + 0] * f[3*4 + 3] - f[3*4 + 0] * f[2*4 + 3];
		double Coef14 = f[1*4 + 0] * f[3*4 + 3] - f[3*4 + 0] * f[1*4 + 3];
		double Coef15 = f[1*4 + 0] * f[2*4 + 3] - f[2*4 + 0] * f[1*4 + 3];

		double Coef16 = f[2*4 + 0] * f[3*4 + 2] - f[3*4 + 0] * f[2*4 + 2];
		double Coef18 = f[1*4 + 0] * f[3*4 + 2] - f[3*4 + 0] * f[1*4 + 2];
		double Coef19 = f[1*4 + 0] * f[2*4 + 2] - f[2*4 + 0] * f[1*4 + 2];

		double Coef20 = f[2*4 + 0] * f[3*4 + 1] - f[3*4 + 0] * f[2*4 + 1];
		double Coef22 = f[1*4 + 0] * f[3*4 + 1] - f[3*4 + 0] * f[1*4 + 1];
		double Coef23 = f[1*4 + 0] * f[2*4 + 1] - f[2*4 + 0] * f[1*4 + 1];

		Vec4d Fac0 = new Vec4d(Coef00, Coef00, Coef02, Coef03);
		Vec4d Fac1 = new Vec4d(Coef04, Coef04, Coef06, Coef07);
		Vec4d Fac2 = new Vec4d(Coef08, Coef08, Coef10, Coef11);
		Vec4d Fac3 = new Vec4d(Coef12, Coef12, Coef14, Coef15);
		Vec4d Fac4 = new Vec4d(Coef16, Coef16, Coef18, Coef19);
		Vec4d Fac5 = new Vec4d(Coef20, Coef20, Coef22, Coef23);

		Vec4d Vec0 = new Vec4d(f[1*4 + 0], f[0*4 + 0], f[0*4 + 0], f[0*4 + 0]);
		Vec4d Vec1 = new Vec4d(f[1*4 + 1], f[0*4 + 1], f[0*4 + 1], f[0*4 + 1]);
		Vec4d Vec2 = new Vec4d(f[1*4 + 2], f[0*4 + 2], f[0*4 + 2], f[0*4 + 2]);
		Vec4d Vec3 = new Vec4d(f[1*4 + 3], f[0*4 + 3], f[0*4 + 3], f[0*4 + 3]);

		Vec4d Inv0 = new Vec4d(
			Vec1.x * Fac0.x - Vec2.x * Fac1.x + Vec3.x * Fac2.x,
			Vec1.y * Fac0.y - Vec2.y * Fac1.y + Vec3.y * Fac2.y,
			Vec1.z * Fac0.z - Vec2.z * Fac1.z + Vec3.z * Fac2.z,
			Vec1.w * Fac0.w - Vec2.w * Fac1.w + Vec3.w * Fac2.w
		);
		Vec4d Inv1 = new Vec4d(
			Vec0.x * Fac0.x - Vec2.x * Fac3.x + Vec3.x * Fac4.x,
			Vec0.y * Fac0.y - Vec2.y * Fac3.y + Vec3.y * Fac4.y,
			Vec0.z * Fac0.z - Vec2.z * Fac3.z + Vec3.z * Fac4.z,
			Vec0.w * Fac0.w - Vec2.w * Fac3.w + Vec3.w * Fac4.w
			// Vec0 * Fac0 - Vec2 * Fac3 + Vec3 * Fac4
		);
		Vec4d Inv2 = new Vec4d(
			Vec0.x * Fac1.x - Vec1.x * Fac3.x + Vec3.x * Fac5.x,
			Vec0.y * Fac1.y - Vec1.y * Fac3.y + Vec3.y * Fac5.y,
			Vec0.z * Fac1.z - Vec1.z * Fac3.z + Vec3.z * Fac5.z,
			Vec0.w * Fac1.w - Vec1.w * Fac3.w + Vec3.w * Fac5.w
			// Vec0 * Fac1 - Vec1 * Fac3 + Vec3 * Fac5
		);
		Vec4d Inv3 = new Vec4d(
			Vec0.x * Fac2.x - Vec1.x * Fac4.x + Vec2.x * Fac5.x,
			Vec0.y * Fac2.y - Vec1.y * Fac4.y + Vec2.y * Fac5.y,
			Vec0.z * Fac2.z - Vec1.z * Fac4.z + Vec2.z * Fac5.z,
			Vec0.w * Fac2.w - Vec1.w * Fac4.w + Vec2.w * Fac5.w
			// Vec0 * Fac2 - Vec1 * Fac4 + Vec2 * Fac5
		);

		Vec4d SignA = new Vec4d(+1, -1, +1, -1);
		Vec4d SignB = new Vec4d(-1, +1, -1, +1);
		double[] inverseFields = new double[] {
			Inv0.x * SignA.x, Inv0.y * SignA.y, Inv0.z * SignA.z, Inv0.w * SignA.w,
			Inv1.x * SignB.x, Inv1.y * SignB.y, Inv1.z * SignB.z, Inv1.w * SignB.w,
			Inv2.x * SignA.x, Inv2.y * SignA.y, Inv2.z * SignA.z, Inv2.w * SignA.w,
			Inv3.x * SignB.x, Inv3.y * SignB.y, Inv3.z * SignB.z, Inv3.w * SignB.w
		};
		Mat4x4d Inverse = new Mat4x4d(inverseFields);

		Vec4d Dot0 = new Vec4d(
			f[0] * inverseFields[0],
			f[1] * inverseFields[4],
			f[2] * inverseFields[8],
			f[3] * inverseFields[12]
			// f[0] * (Row 0 of Inverse)
		);
		double Dot1 = (Dot0.x + Dot0.y) + (Dot0.z + Dot0.w);

		double OneOverDeterminant = 1 / Dot1;

		dest.copyFrom(Inverse);
		dest.scale(OneOverDeterminant);
	}
	
	public static void multiply(IMatd a, IMatd b, IMatd dest)
	{
		int aCols = a.getCols();
		int bCols = b.getCols();
		int aRows = a.getRows();
		int bRows = b.getRows();
		double[] aFields = a.getFields();
		double[] bFields = b.getFields();
		
		if (aCols != bRows)
			return;
		
		int dim  = aCols > aRows ? aRows : aCols;
		
		for (int i = 0; i < bCols; ++i) // Columns
		{
			for (int j = 0; j < aRows; ++j) // Rows
			{
				double dot = 0;
				for (int k = 0; k < aCols; k++)
					dot += aFields[k * aRows + j] * bFields[i * bRows + k];
				dest.set(i, j, dot);
			}
		}
	}
	
}
