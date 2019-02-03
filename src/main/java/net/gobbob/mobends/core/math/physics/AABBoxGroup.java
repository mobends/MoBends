package net.gobbob.mobends.core.math.physics;

public class AABBoxGroup implements ICollider
{

	private AABBox[] boxes;
	
	public AABBoxGroup(IAABBox[] boxes)
	{
		this.boxes = new AABBox[boxes.length];
		for (int i = 0; i < boxes.length; ++i)
		{
			this.boxes[i] = new AABBox(boxes[i]);
		}
	}
	
	@Override
	public RayHitInfo intersect(Ray ray)
	{
		for (AABBox box : this.boxes)
		{
			RayHitInfo info = Physics.intersect(ray, box);
			if (info != null)
				return info;
		}
		return null;
	}
	
}
