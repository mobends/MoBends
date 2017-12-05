package net.gobbob.mobends.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.util.SmoothVector3f;

public interface IModelPart
{
	public void applyTransform(float scale);
	public void renderPart(float scale);
	public void update(float ticksPerFrame);
	public Vector3f getPosition();
	public Vector3f getScale();
	public SmoothVector3f getRotation();
	public SmoothVector3f getPreRotation();
	
	public void syncUp(IModelPart part);
}
