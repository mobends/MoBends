package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.util.GlHelper;

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
		SmoothVector3f smoothing = this.getSmoothOffset();
		if (smoothing.isMoving())
		{
			smoothing.update(ticksPerFrame);
			this.getOffset().set(smoothing.getX(), smoothing.getY(), smoothing.getZ());
		}
	}

	/**
	 * Whether the part turns with its parent. One that doesn't (a biped's head) keeps only where its
	 * parent puts it: its own rotation is relative to the model, not to the parent (see
	 * {@code BoneDefinition#inheritRotation}).
	 */
	default boolean inheritsRotation()
	{
		return true;
	}

	/** The rotation the part adds to its frame: its own, after its rest rotation if it has one. */
	default void getLocalRotation(Quaternion dest)
	{
		dest.set(this.getRotation().getSmooth());
	}

	/**
	 * The rotation of the part's frame relative to the model: its own and its parents', up to the
	 * first that doesn't turn with its parent.
	 */
	default void getModelRotation(Quaternion dest)
	{
		this.getLocalRotation(dest);
		final Quaternion parentRotation = new Quaternion();
		for (IModelPart part = this; part.inheritsRotation() && part.getParent() != null; part = part.getParent())
		{
			part.getParent().getLocalRotation(parentRotation);
			Quaternion.mul(parentRotation, dest, dest);
		}
	}

	/**
	 * For a part that doesn't turn with its parent: turns back by its parents' rotation, so what
	 * follows (its own rotation) is relative to the model. Applied after the part's translation,
	 * which still follows the parent.
	 */
	default void applyParentRotationUndone()
	{
		if (this.inheritsRotation() || this.getParent() == null)
		{
			return;
		}
		final Quaternion parentRotation = new Quaternion();
		this.getParent().getModelRotation(parentRotation);
		parentRotation.conjugate();
		GlHelper.rotate(parentRotation);
	}

	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	IVec3f getOffset();
	/** Where the animators smooth the offset towards its target; {@link #updateOffset} carries it into {@link #getOffset}. */
	SmoothVector3f getSmoothOffset();
	SmoothOrientation getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
