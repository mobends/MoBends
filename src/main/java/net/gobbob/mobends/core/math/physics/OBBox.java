package net.gobbob.mobends.core.math.physics;

import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;

public class OBBox
{
	
	public final Vec3fReadonly min, max;
	public final Mat4x4d transform;
	
	public OBBox(IVec3fRead min, IVec3fRead max, IMat4x4d transform)
	{
		this.min = new Vec3fReadonly(min);
		this.max = new Vec3fReadonly(max);
		this.transform = new Mat4x4d(transform);
	}
	
	public OBBox(float x0, float y0, float z0, float x1, float y1, float z1, IMat4x4d transform)
	{
		this.min = new Vec3fReadonly(x0, y0, z0);
		this.max = new Vec3fReadonly(z1, y1, z1);
		this.transform = new Mat4x4d(transform);
	}
	
}
