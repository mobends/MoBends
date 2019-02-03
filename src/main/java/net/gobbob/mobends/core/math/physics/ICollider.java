package net.gobbob.mobends.core.math.physics;

public interface ICollider
{

	RayHitInfo intersect(Ray ray);
	
}
