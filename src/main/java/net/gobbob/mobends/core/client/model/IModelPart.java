package net.gobbob.mobends.core.client.model;

import net.gobbob.mobends.core.util.IVec3f;
import net.gobbob.mobends.core.util.SmoothOrientation;

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
	public IVec3f getPosition();
	public IVec3f getScale();
	public SmoothOrientation getRotation();
	public boolean isShowing();
	public void setVisible(boolean showModel);
	
	public void syncUp(IModelPart part);
}
