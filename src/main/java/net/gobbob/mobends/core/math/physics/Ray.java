package net.gobbob.mobends.core.math.physics;

import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;

public class Ray
{
	
	public final Vec3fReadonly position;
	public final Vec3fReadonly direction;
	
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
