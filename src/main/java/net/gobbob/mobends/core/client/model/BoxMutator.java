package net.gobbob.mobends.core.client.model;

import java.util.Collection;

import net.gobbob.mobends.core.client.model.BoxFactory.TextureFace;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.math.MathHelper;

public class BoxMutator
{
	
	protected ModelBase targetModel;
	protected ModelRenderer targetRenderer;
	protected BoxFactory factory;
	
	protected int textureOffsetX;
	protected int textureOffsetY;
	protected float globalBoxX, globalBoxY, globalBoxZ;
	
	public BoxMutator(ModelBase targetModel, ModelRenderer targetRenderer, BoxFactory factory, int textureOffsetX, int textureOffsetY)
	{
		this.targetModel = targetModel;
		this.targetRenderer = targetRenderer;
		this.factory = factory;
		this.textureOffsetX = textureOffsetX;
		this.textureOffsetY = textureOffsetY;
		
		this.globalBoxX = this.targetRenderer.rotationPointX + this.factory.min.x;
		this.globalBoxY = this.targetRenderer.rotationPointY + this.factory.min.y;
		this.globalBoxZ = this.targetRenderer.rotationPointZ + this.factory.min.z;
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
		
		float x = original.posX1;
		float y = original.posY1;
		float z = original.posZ1;
		int width = (int) (original.posX2 - original.posX1);
		int height = (int) (original.posY2 - original.posY1);
		int length = (int) (original.posZ2 - original.posZ1);
		
		float textureWidth = modelRenderer.textureWidth;
		float textureHeight = modelRenderer.textureHeight;
		int texU = (int)(quadList[MutatedBox.RIGHT].vertexPositions[1].texturePositionX * textureWidth);
		int texV = 0;
		if (modelRenderer.mirror)
			texV = (int)(quadList[MutatedBox.BOTTOM].vertexPositions[1].texturePositionY * textureHeight);
		else
			texV = (int)(quadList[MutatedBox.TOP].vertexPositions[1].texturePositionY * textureHeight);
		
		float inflation1 = Math.abs((float)(original.posX1 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation2 = Math.abs((float)(original.posX2 - quadList[1].vertexPositions[0].vector3D.x));
		float inflation = Math.min(inflation1, inflation2);
		
		BoxFactory target = new BoxFactory(modelRenderer, original);
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
	
	public float getGlobalBoxX()
	{
		return this.globalBoxX;
	}
	
	public float getGlobalBoxY()
	{
		return this.globalBoxY;
	}
	
	public float getGlobalBoxZ()
	{
		return this.globalBoxZ;
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
	
	public void includeParentTransform(Collection<ModelRenderer> parentsList)
	{
		for (ModelRenderer parent : parentsList)
		{
			this.includeParentTransform(parent);
		}
	}
	
	/*
	 * This will move the box to a new location, so that when the new
	 * origin will be applied, it will stay in the same place.
	 * 
	 * The origin is in model space (aka. global space)
	 */
	public void offsetBasedOnNewOrigin(float originX, float originY, float originZ)
	{
		float offsetX = originX - this.globalBoxX;
		float offsetY = originY - this.globalBoxY;
		float offsetZ = originZ - this.globalBoxZ;
		
		this.factory.offset(-offsetX, -offsetY, -offsetZ);
	}
	
	/*
	 * This will reverse the effect of the offsetBasedOnNewOrigin function.
	 * 
	 * The origin is in model space (aka. global space)
	 */
	public void offsetBackBasedOnNewOrigin(float originX, float originY, float originZ)
	{
		float offsetX = originX - this.globalBoxX;
		float offsetY = originY - this.globalBoxY;
		float offsetZ = originZ - this.globalBoxZ;
		
		this.factory.offset(offsetX, offsetY, offsetZ);
	}
	
	public BoxFactory sliceFromBottom(float sliceY, boolean preservePositions)
	{
		final float height = this.factory.max.y - this.factory.min.y;
		final float localSliceY = sliceY - this.globalBoxY;
		
		// If slicing is necessarry (if the cut plane intersects the box)
		if (localSliceY > this.factory.min.getY() && localSliceY < this.factory.max.getY())
		{
			final float newHeight = localSliceY - this.factory.min.getY();
			
			factory.max.setY(localSliceY);
			factory.hideFace(BoxSide.BOTTOM);
			
			TextureFace[] slidesFaces = new TextureFace[6];
			BoxSide[] faces = { BoxSide.BACK, BoxSide.FRONT, BoxSide.LEFT, BoxSide.RIGHT };
			for (BoxSide faceEnum : faces)
			{
				float textureOffset = newHeight / height;
				
				TextureFace face = this.factory.faces[faceEnum.faceIndex];
				int sliceV = (int) (face.v0 + (face.v1 - face.v0) * textureOffset);
				slidesFaces[faceEnum.faceIndex] = new TextureFace(face.u0, sliceV, face.u1, face.v1);
				face.v1 = sliceV;
			}
			slidesFaces[BoxSide.TOP.faceIndex] = new TextureFace(this.factory.faces[BoxSide.TOP.faceIndex]);
			slidesFaces[BoxSide.BOTTOM.faceIndex] = new TextureFace(this.factory.faces[BoxSide.BOTTOM.faceIndex]);
			
			float slicedY = preservePositions ? localSliceY : 0;
			BoxFactory sliced = new BoxFactory(factory.min.x, localSliceY, factory.min.z, factory.max.x, factory.max.y, factory.max.z, slidesFaces);
			
			sliced.offsetTextureQuad(BoxSide.BOTTOM, 0, -(int)newHeight);
			sliced.hideFace(BoxSide.TOP);
			
			return sliced;
		}
		
		// Nothing was cut
		return null;
	}
	
}
