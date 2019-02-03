package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.math.physics.AABBox;
import net.gobbob.mobends.core.math.physics.IAABBox;

public class BoneMetadata
{

	protected AABBox boundingBox;
	
	public BoneMetadata(IAABBox boundingBox)
	{
		this.boundingBox = new AABBox(boundingBox);
	}
	
	public BoneMetadata(float x0, float y0, float z0, float x1, float y1, float z1)
	{
		this.boundingBox = new AABBox(x0, y0, z0, x1, y1, z1);
	}
	
	public IAABBox getBounds()
	{
		return this.boundingBox;
	}
	
}
