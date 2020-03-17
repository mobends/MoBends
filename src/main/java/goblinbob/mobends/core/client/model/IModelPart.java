package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.math.vector.IVec3f;

public interface IModelPart
{
	
	void applyStandaloneTransform(float scale);
	void applyOwnTransform(float scale);
	// Made to transform and propagate downwards the stream of children parts.
	default void propagateTransform(float scale)
	{
		this.applyOwnTransform(scale);
		this.applyPostTransform(scale);
	}
	/*
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it gets that rotation
	 */
	void applyPostTransform(float scale);
	
	void renderPart(float scale);
	void renderJustPart(float scale);
	void update(float ticksPerFrame);
	void applyLocalSpaceTransform(float scale, IMat4x4d dest);
	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	SmoothOrientation getRotation();
	IModelPart getParent();
	boolean isShowing();
	
	default void applyCharacterSpaceTransform(float scale, IMat4x4d matrix)
	{
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterSpaceTransform(scale, matrix);
			this.applyLocalSpaceTransform(scale, matrix);
		}
		else
		{
			this.applyLocalSpaceTransform(scale, matrix);
		}
	}
	
	
}
