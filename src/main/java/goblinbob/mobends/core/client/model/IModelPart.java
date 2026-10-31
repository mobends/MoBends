package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.SmoothVector3f;

public interface IModelPart
{

	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param scale Controls the scale of the translation.
	 */
	void applyPreTransform(float scale);

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param scale Controls the scale of the translation.
	 */
	void applyLocalTransform(float scale);

	/**
	 * Applies the transform in character space (includes all parents).
	 * @param scale Controls the scale of the translation.
	 */
	default void applyCharacterTransform(float scale)
	{
		this.applyPreTransform(scale);
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(scale * getOffsetScale());
		}
		this.applyLocalTransform(scale);
	}

	/**
	 * Made to transform and propagate downwards the stream of children parts.
	 * @param scale Controls the scale of the translation.
 	 */
	default void propagateTransform(float scale)
	{
		this.applyLocalTransform(scale);
		this.applyPostTransform(scale);
	}

	/**
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it (the children) gets that rotation.
	 */
	void applyPostTransform(float scale);
	
	void renderPart(float scale);
	void renderJustPart(float scale);
	void update(float ticksPerFrame);

	/** Moves the offset along its smoothing, while it has somewhere to go; called from {@link #update}. */
	default void updateOffset(float ticksPerFrame)
	{
		SmoothVector3f smoothing = this.getOffsetSmoothing();
		if (smoothing.isMoving())
		{
			smoothing.update(ticksPerFrame);
			this.getOffset().set(smoothing.getX(), smoothing.getY(), smoothing.getZ());
		}
	}

	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	IVec3f getOffset();
	/** Where the animators smooth the offset towards its target; {@link #updateOffset} carries it into {@link #getOffset}. */
	SmoothVector3f getOffsetSmoothing();
	SmoothOrientation getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
