package net.gobbob.mobends.core.math;

import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;

public class Ray
{
	
	private Vec3fReadonly position;
	private Vec3fReadonly direction;
	
	public Ray(IVec3fRead position, IVec3fRead direction)
	{
		this.position = new Vec3fReadonly(position);
		this.direction = new Vec3fReadonly(direction);
	}
	
	public Ray(float posX, float posY, float posZ, float dirX, float dirY, float dirZ)
	{
		this.position = new Vec3fReadonly(posX, posY, posZ);
		this.direction = new Vec3fReadonly(dirX, dirY, dirZ);
	}
	
	public IVec3fRead getPosition()
	{
		return this.position;
	}
	
	public IVec3fRead getDirection()
	{
		return this.direction;
	}
	
}
