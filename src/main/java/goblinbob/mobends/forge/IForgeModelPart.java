package goblinbob.mobends.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import goblinbob.mobends.core.IModelPart;

public interface IForgeModelPart extends IModelPart
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

	void setParent(IForgeModelPart parent);
	void addChild(ModelPart child);
	IForgeModelPart getParent();
}
