package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.math.vector.IVec3f;

public interface IModelPart
{

	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param scale Controls the scale of the translation.
	 */
	void applyPreTransform(float scale);

	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	void applyPreTransform(float scale, IMat4x4d dest);

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param scale Controls the scale of the translation.
	 */
	void applyLocalTransform(float scale);

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	void applyLocalTransform(float scale, IMat4x4d dest);

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
	 * Applies the transform in character space (includes all parents).
	 * @param scale Controls the scale of the translation.
	 * @param dest The matrix to be transformed.
	 */
	default void applyCharacterTransform(float scale, IMat4x4d dest)
	{
		this.applyPreTransform(scale, dest);
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(scale * getOffsetScale(), dest);
		}
		this.applyLocalTransform(scale, dest);
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
	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	IVec3f getOffset();
	SmoothOrientation getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
