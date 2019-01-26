package net.gobbob.mobends.core.math.physics;

import javax.annotation.Nullable;

import net.gobbob.mobends.core.math.vector.IVec3f;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.VectorUtils;

public class Physics
{
	
	@Nullable
	public static RayHitInfo castRay(Ray ray, Plane plane)
	{
		IVec3fRead rayOrigin = ray.getPosition();
		IVec3fRead rayDirection = ray.getDirection();
		IVec3fRead planeOrigin = plane.getPosition();
		IVec3fRead planeNormal = plane.getNormal();
		
		Vec3f rayToPlane = VectorUtils.subtract(planeOrigin, rayOrigin);
		
		float rayToPlaneDot = (float) VectorUtils.dot(rayToPlane, planeNormal);
		float rayToEndDot = (float) VectorUtils.dot(rayDirection, planeNormal);
	
		if (rayToEndDot != 0)
		{
			float ratio = rayToPlaneDot / rayToEndDot;
			if (ratio > 0)
			{
				return new RayHitInfo(
					rayOrigin.getX() + rayDirection.getX() * ratio,
					rayOrigin.getY() + rayDirection.getY() * ratio,
					rayOrigin.getZ() + rayDirection.getZ() * ratio
				);
			}
		}
		
		return null;
	}
	
}
