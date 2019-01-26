package net.gobbob.mobends.core.math.physics;

import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;

public class Plane
{
	
	private Vec3fReadonly position;
	private Vec3fReadonly normal;
	
	public Plane(IVec3fRead position, IVec3fRead normal)
	{
		this.position = new Vec3fReadonly(position);
		this.normal = new Vec3fReadonly(normal);
	}
	
	public Plane(float posX, float posY, float posZ, float dirX, float dirY, float dirZ)
	{
		this.position = new Vec3fReadonly(posX, posY, posZ);
		this.normal = new Vec3fReadonly(dirX, dirY, dirZ);
	}
	
	public IVec3fRead getPosition()
	{
		return this.position;
	}
	
	public IVec3fRead getNormal()
	{
		return this.normal;
	}
	
}
