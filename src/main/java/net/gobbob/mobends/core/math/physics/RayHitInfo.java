package net.gobbob.mobends.core.math.physics;

import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;

public class RayHitInfo
{
	
	public final Vec3fReadonly hitPoint;
	
	public RayHitInfo(IVec3fRead hitPoint)
	{
		this.hitPoint = new Vec3fReadonly(hitPoint);
	}
	
	public RayHitInfo(float x, float y, float z)
	{
		this.hitPoint = new Vec3fReadonly(x, y, z);
	}
	
}
