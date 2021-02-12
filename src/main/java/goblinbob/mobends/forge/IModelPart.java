package goblinbob.mobends.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import net.minecraft.util.math.vector.Quaternion;

public interface IModelPart
{
	/**
	 * Applies the transform in global space (relative to the entity).
	 * This is being applied before the parent transform.
	 * @param matrix The matrix to be transformed.
	 * @param scale Controls the scale of the translation.
	 */
	default void applyPreTransform(MatrixStack matrix, double scale) {}

	/**
	 * Applies the transform in local space (relative to the parent).
	 * @param matrix The matrix to be transformed.
	 * @param scale Controls the scale of the translation.
	 */
	void applyLocalTransform(MatrixStack matrix, double scale);

	/**
	 * Applies the transform in character space (includes all parents).
	 * @param matrix The matrix to be transformed.
	 * @param scale Controls the scale of the translation.
	 */
	default void applyCharacterTransform(MatrixStack matrix, double scale)
	{
		this.applyPreTransform(matrix, scale);
		if (this.getParent() != null)
		{
			this.getParent().applyCharacterTransform(matrix, scale * getOffsetScale());
		}
		this.applyLocalTransform(matrix, scale);
	}

	/**
	 * Made to transform and propagate downwards the stream of children parts.
	 * @param matrix The matrix to be transformed.
	 * @param scale Controls the scale of the translation.
 	 */
	default void propagateTransform(MatrixStack matrix, double scale)
	{
		this.applyLocalTransform(matrix, scale);
		this.applyPostTransform(matrix, scale);
	}

	/**
	 * This transform is applied after rendering the part, so that
	 * whatever is rendered after it (the children) gets that rotation.
	 */
	default void applyPostTransform(MatrixStack matrix, double scale) {}
	
	void syncUp(IModelPart part);
	void setVisible(boolean showModel);
	IVec3f getPosition();
	IVec3f getScale();
	IVec3f getOffset();
	Quaternion getRotation();
	float getOffsetScale();
	IVec3f getGlobalOffset();
	IModelPart getParent();
	boolean isShowing();

}
