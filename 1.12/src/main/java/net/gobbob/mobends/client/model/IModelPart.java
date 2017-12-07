package net.gobbob.mobends.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.util.SmoothVector3f;

public interface IModelPart
{
	public void applyStandaloneTransform(float scale);
	public void applyOwnTransform(float scale);
	//Made to transform and propagate downwards the stream of children parts.
	public void propagateTransform(float scale);
	/*
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it gets that rotation
	 */
	public void applyPostTransform(float scale);
	
	public void renderPart(float scale);
	public void renderJustPart(float scale);
	public void update(float ticksPerFrame);
	public Vector3f getPosition();
	public Vector3f getScale();
	public SmoothVector3f getRotation();
	public SmoothVector3f getPreRotation();
	public boolean isShowing();
	
	public void syncUp(IModelPart part);
}
