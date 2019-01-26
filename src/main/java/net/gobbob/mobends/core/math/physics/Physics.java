package net.gobbob.mobends.core.math.physics;

import javax.annotation.Nullable;

import net.gobbob.mobends.core.math.vector.IVec3f;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.VectorUtils;

public class Physics
{
	
	@Nullable
	public static RayHitInfo intersect(Ray ray, Plane plane)
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
	
	public static RayHitInfo intersect(Ray ray, float maxDistance, AABBox box)
	{
		final float rayX = ray.position.getX();
		final float rayY = ray.position.getY();
		final float rayZ = ray.position.getZ();
		final float dirX = ray.direction.getX() * maxDistance;
		final float dirY = ray.direction.getY() * maxDistance;
		final float dirZ = ray.direction.getZ() * maxDistance;
		final float dirInvX = 1 / dirX;
		final float dirInvY = 1 / dirY;
		final float dirInvZ = 1 / dirZ;
		
		float tMin, min, tMax, max;
		float[] b = new float[] {
			dirInvX < 0 ? 1 : 0,
			dirInvY < 0 ? 1 : 0,
			dirInvZ < 0 ? 1 : 0,
		};
		
		// X Axis
		tMin = ((dirInvX < 0 ? box.max.getX() : box.min.getX()) - rayX) * dirInvX;
		tMax = ((dirInvX < 0 ? box.min.getX() : box.max.getX()) - rayX) * dirInvX;
		
		// Y Axis
		min = ((dirInvY < 0 ? box.max.getY() : box.min.getY()) - rayY) * dirInvY;
		max = ((dirInvY < 0 ? box.min.getY() : box.max.getY()) - rayY) * dirInvY;
		
		if (max < tMin || min > tMax) return null;
		if (min > tMin) tMin = min;
		if (max < tMax) tMax = max;
		
		// Z Axis
		min = ((dirInvZ < 0 ? box.max.getZ() : box.min.getZ()) - rayZ) * dirInvZ;
		max = ((dirInvZ < 0 ? box.min.getZ() : box.max.getZ()) - rayZ) * dirInvZ;
		
		if (max < tMin || min > tMax) return null;
		if (min > tMin) tMin = min;
		if (max < tMax) tMax = max;
		
		//float txMin = (box.min.getX() - ray.position.getX()) / dirX;
		//float txMax = (box.max.getX() - ray.position.getX()) / dirX;
		
		return new RayHitInfo(rayX + dirX * tMin, rayY + dirY * tMin, rayZ + dirZ * tMin);
	}
	
}
