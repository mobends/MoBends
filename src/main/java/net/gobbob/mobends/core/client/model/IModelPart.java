package net.gobbob.mobends.core.client.model;

import net.gobbob.mobends.core.math.SmoothOrientation;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.matrix.MatrixUtils;
import net.gobbob.mobends.core.math.vector.IVec3f;

public interface IModelPart
{
	
	void applyStandaloneTransform(float scale);
	void applyOwnTransform(float scale);
	// Made to transform and propagate downwards the stream of children parts.
	void propagateTransform(float scale);
	/*
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it gets that rotation
	 */
	void applyPostTransform(float scale);
	
	void renderPart(float scale);
	void renderJustPart(float scale);
	void update(float ticksPerFrame);
	IVec3f getPosition();
	IVec3f getScale();
	SmoothOrientation getRotation();
	IModelPart getParent();
	void getLocalTransform(float scale, IMat4x4d dest);
	
	boolean isShowing();
	void setVisible(boolean showModel);
	
	void syncUp(IModelPart part);
	
	default void getWorldTransform(float scale, IMat4x4d dest)
	{
		if (this.getParent() != null)
		{
			this.getParent().getWorldTransform(scale, dest);
			Mat4x4d local = new Mat4x4d();
			this.getLocalTransform(scale, local);
			MatrixUtils.multiply(dest, local, dest);
		}
		else
		{
			this.getLocalTransform(scale, dest);
		}
	}
	
}
