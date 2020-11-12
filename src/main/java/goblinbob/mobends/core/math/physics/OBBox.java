package goblinbob.mobends.core.math.physics;

import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.math.matrix.Mat4x4d;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;

public class OBBox implements IOBBox, ICollider
{
	
	public Vec3f min;
	public Vec3f max;
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
		this.max = new Vec3f(x1, y1, z1);
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

	public Vec3f getCenter()
	{
		return new Vec3f(
			(this.min.x + this.max.x) / 2,
			(this.min.y + this.max.y) / 2,
			(this.min.z + this.max.z) / 2
		);
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
