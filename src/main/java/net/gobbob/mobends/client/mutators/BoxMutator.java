package net.gobbob.mobends.client.mutators;

import java.lang.reflect.Field;

import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.FieldMiner;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.math.MathHelper;

public class BoxMutator
{
	protected ModelBase targetModel;
	protected ModelRenderer targetRenderer;
	protected ModelBox targetBox;
	
	protected int textureOffsetX;
	protected int textureOffsetY;
	protected float globalBoxX, globalBoxY, globalBoxZ;
	
	public BoxMutator(ModelBase targetModel, ModelRenderer targetRenderer, ModelBox target, int textureOffsetX, int textureOffsetY)
	{
		this.targetModel = targetModel;
		this.targetRenderer = targetRenderer;
		this.targetBox = target;
		this.textureOffsetX = textureOffsetX;
		this.textureOffsetY = textureOffsetY;
		
		this.globalBoxX = this.targetRenderer.rotationPointX + this.targetBox.posX1;
		this.globalBoxY = this.targetRenderer.rotationPointY + this.targetBox.posY1;
		this.globalBoxZ = this.targetRenderer.rotationPointZ + this.targetBox.posZ1;
	}
	
	/*
	 * It creates a BoxMutator with a copy of the original model, that can be mutated.
	 * The original stays in it's original state.
	 */
	public static BoxMutator createFrom(final ModelBase modelBase, final ModelRenderer modelRenderer, final net.minecraft.client.model.ModelBox original)
	{
		Field quadListField = FieldMiner.getObfuscatedField(original.getClass(), "quadList", "field_78254_i");
		
		if(quadListField == null)
		{
			return null;
		}
		
		TexturedQuad[] quadList = null;
		
		try
		{
			quadListField.setAccessible(true);
			quadList = (TexturedQuad[]) quadListField.get(original);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		float x = original.posX1;
		float y = original.posY1;
		float z = original.posZ1;
		int width = (int) (original.posX2 - original.posX1);
		int height = (int) (original.posY2 - original.posY1);
		int length = (int) (original.posZ2 - original.posZ1);
		
		if (quadList == null)
		{
			return null;
		}
		
		float textureWidth = modelRenderer.textureWidth;
		float textureHeight = modelRenderer.textureHeight;
		int texU = (int)(quadList[ModelBox.RIGHT].vertexPositions[1].texturePositionX * textureWidth);
		int texV = 0;
		if (modelRenderer.mirror)
			texV = (int)(quadList[ModelBox.BOTTOM].vertexPositions[1].texturePositionY * textureHeight);
		else
			texV = (int)(quadList[ModelBox.TOP].vertexPositions[1].texturePositionY * textureHeight);
		
		float inflation1 = Math.abs((float)(original.posX1 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation2 = Math.abs((float)(original.posX2 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation = Math.min(inflation1, inflation2);
		
		ModelBox target = new ModelBox(modelRenderer, texU, texV, x, y, z, width, height, length, inflation);
		return new BoxMutator(modelBase, modelRenderer, target, texU, texV);
	}

	public ModelBox getTargetBox()
	{
		return this.targetBox;
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
	 * Offsets the global position of this box to include what it's
	 * parent would offset it by. This is helpful when slicing the target
	 * box into mutliple boxes, so that the slice plane matches the model
	 * space.
	 */
	public void includeParentTransform(ModelRenderer parentRenderer)
	{
		this.globalBoxX += parentRenderer.rotationPointX;
		this.globalBoxY += parentRenderer.rotationPointY;
		this.globalBoxZ += parentRenderer.rotationPointZ;
	}
	
	/*
	 * This will move the box to a new location, so that when the new
	 * origin will be applied, it will stay in the same place.
	 * 
	 * The origin is in model space (aka. global space)
	 */
	public void offsetBasedOnNewOrigin(float originX, float originY, float originZ)
	{
		//TODO Implement it fully, if it hasn't been yet.
		float offsetX = originX - this.globalBoxX;
		float offsetY = originY - this.globalBoxY;
		float offsetZ = originZ - this.globalBoxZ;
		
		this.targetBox.offset(-offsetX, -offsetY, -offsetZ);
	}
	
	public ModelBox sliceFromBottom(float sliceY, boolean preservePositions)
	{
		float height = targetBox.height;
		
		float localY = sliceY - this.globalBoxY;
		// If slicing is necessarry (if the cut plane intersects the box)
		if (localY > 0 && localY < height)
		{
			targetBox.height = localY - targetBox.inflation;
			// Changing the original height to alter the texture
			// to not stretch it.
			targetBox.originalHeight = MathHelper.floor(localY);
			targetBox.updateVertices(this.targetRenderer);
			targetBox.setVisibility(ModelBox.BOTTOM, false);
			
			float slicedY = preservePositions ? (this.targetBox.posY1 + localY) : 0;
			ModelBox sliced = new ModelBox(targetRenderer, this.textureOffsetX, this.textureOffsetY + (int)localY, this.targetBox.posX1, slicedY + targetBox.inflation, this.targetBox.posZ1,
														   (int)targetBox.width, (int)(height - localY), (int)targetBox.length, targetBox.inflation);
			sliced.height -= targetBox.inflation;
			sliced.updateVertices(this.targetRenderer);
			sliced.offsetTextureQuad(targetRenderer, ModelBox.BOTTOM, 0, -(int)localY);
			sliced.setVisibility(ModelBox.TOP, false);
			
			return sliced;
		}
		
		// Nothing was cut
		return null;
	}
}
