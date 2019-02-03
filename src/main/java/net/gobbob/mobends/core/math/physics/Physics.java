package net.gobbob.mobends.core.math.physics;

import javax.annotation.Nullable;

import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.vector.IVec3f;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3d;
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
	
	public static RayHitInfo intersect(Ray ray, AABBox box)
	{
		final float rayX = ray.position.getX();
		final float rayY = ray.position.getY();
		final float rayZ = ray.position.getZ();
		final float dirX = ray.direction.getX();
		final float dirY = ray.direction.getY();
		final float dirZ = ray.direction.getZ();
		final float dirInvX = 1 / dirX;
		final float dirInvY = 1 / dirY;
		final float dirInvZ = 1 / dirZ;
		
		float tMin, min, tMax, max;
		
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
		
		return new RayHitInfo(rayX + dirX * tMin, rayY + dirY * tMin, rayZ + dirZ * tMin);
	}
	
	public static RayHitInfo intersect(Ray ray, OBBox box)
	{
		final double[] fields = box.transform.getFields();
		final float rayX = ray.position.getX();
		final float rayY = ray.position.getY();
		final float rayZ = ray.position.getZ();
		final float boxX = (float) fields[12];
		final float boxY = (float) fields[13];
		final float boxZ = (float) fields[14];
		final Vec3f right = new Vec3f((float) fields[0], (float) fields[1], (float) fields[2]);
		final Vec3f up = new Vec3f((float) fields[4], (float) fields[5], (float) fields[6]);
		final Vec3f forward = new Vec3f((float) fields[8], (float) fields[9], (float) fields[10]);
		float scaleX = right.length();
		float scaleY = up.length();
		float scaleZ = forward.length();
		VectorUtils.normalize(right);
		VectorUtils.normalize(up);
		VectorUtils.normalize(forward);
		
		Vec3f rayToBox = new Vec3f(boxX - rayX, boxY - rayY, boxZ - rayZ);
		float nomLen, denomLen;
		float tMin = 0F, tMax = Float.POSITIVE_INFINITY;
		
		// X Axis
		nomLen = VectorUtils.dot(right,  rayToBox);
		denomLen = VectorUtils.dot(ray.direction, right);
		
		if (Math.abs(denomLen) > 0.00001)
		{
			float min = (nomLen + box.min.getX() * scaleX) / denomLen;
			float max = (nomLen + box.max.getX() * scaleX) / denomLen;
			
			if (min > max)
			{
				float t = min;
				min = max;
				max = t;
			}
			
			if (max < tMax) tMax = max;
			if (min > tMin) tMin = min;
			
			if (tMax < tMin) return null;
		}
		else
		{
			if (-nomLen + box.min.getX() > 0 || -nomLen + box.max.getX() < 0)
				return null;
		}
		
		// Y Axis
		nomLen = VectorUtils.dot(up, rayToBox);
		denomLen = VectorUtils.dot(ray.direction, up);
		
		if (Math.abs(denomLen) > 0.00001)
		{
			float min = (nomLen + box.min.getY() * scaleY) / denomLen;
			float max = (nomLen + box.max.getY() * scaleY) / denomLen;
			
			if (min > max)
			{
				float t = min;
				min = max;
				max = t;
			}
			
			if (max < tMax) tMax = max;
			if (min > tMin) tMin = min;
			
			if (tMax < tMin) return null;
		}
		else
		{
			if (-nomLen + box.min.getY() > 0 || -nomLen + box.max.getY() < 0)
				return null;
		}
		
		// Z Axis
		nomLen = VectorUtils.dot(forward, rayToBox);
		denomLen = VectorUtils.dot(ray.direction, forward);
		
		if (Math.abs(denomLen) > 0.00001)
		{
			float min = (nomLen + box.min.getZ() * scaleZ) / denomLen;
			float max = (nomLen + box.max.getZ() * scaleZ) / denomLen;
			
			if (min > max)
			{
				float t = min;
				min = max;
				max = t;
			}
			
			if (max < tMax) tMax = max;
			if (min > tMin) tMin = min;
			
			if (tMax < tMin) return null;
		}
		else
		{
			if (-nomLen + box.min.getZ() > 0 || -nomLen + box.max.getZ() < 0)
				return null;
		}
		
		return new RayHitInfo(rayX + ray.direction.getX() * tMin, rayY + ray.direction.getY() * tMin, rayZ + ray.direction.getZ() * tMin);
	}
	
}
