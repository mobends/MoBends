package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.client.model.BoxFactory.TextureFace;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;

public class BoxMutator
{
	protected ModelBase targetModel;
	protected ModelRenderer targetRenderer;
	protected BoxFactory factory;
	
	protected int textureOffsetX;
	protected int textureOffsetY;
	
	public BoxMutator(ModelBase targetModel, ModelRenderer targetRenderer, BoxFactory factory, int textureOffsetX, int textureOffsetY)
	{
		this.targetModel = targetModel;
		this.targetRenderer = targetRenderer;
		this.factory = factory;
		this.textureOffsetX = textureOffsetX;
		this.textureOffsetY = textureOffsetY;
	}
	
	/*
	 * It creates a BoxMutator with a copy of the original model, that can be mutated.
	 * The original stays in it's original state.
	 */
	public static BoxMutator createFrom(final ModelBase modelBase, final ModelRenderer modelRenderer, final ModelBox original)
	{
		TexturedQuad[] quadList = original.quadList;
		if (quadList == null)
		{
			return null;
		}

		float textureWidth = modelRenderer.textureWidth;
		float textureHeight = modelRenderer.textureHeight;
		int texU = (int)(quadList[MutatedBox.RIGHT].vertexPositions[1].texturePositionX * textureWidth);
		int texV = (int)(quadList[MutatedBox.TOP].vertexPositions[1].texturePositionY * textureHeight);

		if (modelRenderer.mirror)
			texV = (int)(quadList[MutatedBox.BOTTOM].vertexPositions[1].texturePositionY * textureHeight);

		float inflation1 = Math.abs((float) (original.posX1 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation2 = Math.abs((float) (original.posX2 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation = Math.min(inflation1, inflation2);
		
		BoxFactory target = new BoxFactory(modelRenderer, original);
		target.inflate(inflation, inflation, inflation);
		return new BoxMutator(modelBase, modelRenderer, target, texU, texV);
	}

	public BoxFactory getFactory()
	{
		return this.factory;
	}
	
	public int getTextureOffsetX()
	{
		return this.textureOffsetX;
	}
	
	public int getTextureOffsetY()
	{
		return this.textureOffsetY;
	}
	
	/*
	 * This will move the box to a new location.
	 */
	public void offsetBy(float offsetX, float offsetY, float offsetZ)
	{
		this.factory.offset(offsetX, offsetY, offsetZ);
	}
	
	/**
	 * 
	 * @param sliceY Position of the slice relative to the parent modelRenderer.
	 * @return
	 */
	public BoxFactory sliceFromBottom(float sliceY)
	{
		final float height = this.factory.max.y - this.factory.min.y;
		
		// If slicing is necessarry (if the cut plane intersects the box)
		if (sliceY > this.factory.min.y && sliceY < this.factory.max.y)
		{
			final float newHeight = sliceY - this.factory.min.y;

			final TextureFace[] newBoxFaces = new TextureFace[6];
			final BoxSide[] faces = { BoxSide.BACK, BoxSide.FRONT, BoxSide.LEFT, BoxSide.RIGHT };
			for (BoxSide faceEnum : faces)
			{
				final float textureScale = newHeight / height;

				final TextureFace face = this.factory.faces[faceEnum.faceIndex];
				int vSizeSlice = (int) (face.vSize * textureScale);
				
				newBoxFaces[faceEnum.faceIndex] = new TextureFace(face.uPos, face.vPos + vSizeSlice, face.uSize, face.vSize - vSizeSlice);
				face.vSize = vSizeSlice;
			}

			newBoxFaces[BoxSide.TOP.faceIndex] = new TextureFace(this.factory.faces[BoxSide.TOP.faceIndex]);
			newBoxFaces[BoxSide.BOTTOM.faceIndex] = new TextureFace(this.factory.faces[BoxSide.BOTTOM.faceIndex]);

			// Create the sliced box
			final BoxFactory sliced = new BoxFactory(factory.min.x, sliceY, factory.min.z, factory.max.x, factory.max.y, factory.max.z, newBoxFaces);
			sliced.hideFace(BoxSide.TOP);

			// Shorten the original part
			factory.max.setY(sliceY);
			factory.hideFace(BoxSide.BOTTOM);
			
			return sliced;
		}
		
		// Nothing was cut
		return null;
	}
}
