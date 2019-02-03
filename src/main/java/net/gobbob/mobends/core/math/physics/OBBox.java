package net.gobbob.mobends.core.math.physics;

import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;

public class OBBox implements IOBBox, ICollider
{
	
	public Vec3f min, max;
	public Mat4x4d transform;
	
	public OBBox(IVec3fRead min, IVec3fRead max, IMat4x4d transform)
	{
		this.min = new Vec3f(min);
		this.max = new Vec3f(max);
		this.transform = new Mat4x4d(transform);
	}
	
	public OBBox(float x0, float y0, float z0, float x1, float y1, float z1, IMat4x4d transform)
	{
		this.min = new Vec3f(x0, y0, z0);
		this.max = new Vec3f(z1, y1, z1);
		this.transform = new Mat4x4d(transform);
	}
	
	public OBBox(IAABBox aabb)
	{
		this.min = new Vec3f(aabb.getMin());
		this.max = new Vec3f(aabb.getMax());
		this.transform = new Mat4x4d(Mat4x4d.IDENTITY);
	}
	
	public IVec3fRead getMin()
	{
		return this.min;
	}
	
	public IVec3fRead getMax()
	{
		return this.max;
	}
	
	public IMat4x4d getTransform()
	{
		return this.transform;
	}

	@Override
	public RayHitInfo intersect(Ray ray)
	{
		return Physics.intersect(ray, this);
	}
	
}
