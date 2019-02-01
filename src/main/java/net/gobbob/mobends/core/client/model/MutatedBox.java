package net.gobbob.mobends.core.client.model;

import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;

public class MutatedBox extends net.minecraft.client.model.ModelBox
{
	
	// We just need 6 bits (6 faces)
	protected final byte faceVisibilityFlag;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	public static final int FRONT = 4;
	public static final int BACK = 5;
	
	public MutatedBox(ModelRenderer renderer, IVec3fRead min, IVec3fRead max, BoxFactory.TextureFace[] faces, byte faceVisibilityFlag)
	{
		super(renderer, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
		this.faceVisibilityFlag = faceVisibilityFlag;
		
		float x0 = min.getX();
        float y0 = min.getY();
        float z0 = min.getZ();
		float x1 = max.getX();
        float y1 = max.getY();
        float z1 = max.getZ();
        
        if (renderer.mirror)
        {
            float temp = x1;
            x1 = x0;
            x0 = temp;
        }
        
        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x0, y0, z0, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(x1, y0, z0, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(x1, y1, z0, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x0, y1, z0, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x0, y0, z1, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(x1, y0, z1, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(x1, y1, z1, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x0, y1, z1, 8.0F, 0.0F);
        this.vertexPositions[0] = positiontexturevertex7;
        this.vertexPositions[1] = positiontexturevertex;
        this.vertexPositions[2] = positiontexturevertex1;
        this.vertexPositions[3] = positiontexturevertex2;
        this.vertexPositions[4] = positiontexturevertex3;
        this.vertexPositions[5] = positiontexturevertex4;
        this.vertexPositions[6] = positiontexturevertex5;
        this.vertexPositions[7] = positiontexturevertex6;
        this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, faces[0].u0, faces[0].v0, faces[0].u1, faces[0].v1, renderer.textureWidth, renderer.textureHeight);
        this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, faces[1].u0, faces[1].v0, faces[1].u1, faces[1].v1, renderer.textureWidth, renderer.textureHeight);
        this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, faces[2].u0, faces[2].v0, faces[2].u1, faces[2].v1, renderer.textureWidth, renderer.textureHeight);
        this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, faces[3].u0, faces[3].v0, faces[3].u1, faces[3].v1, renderer.textureWidth, renderer.textureHeight);
        this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, faces[4].u0, faces[4].v0, faces[4].u1, faces[4].v1, renderer.textureWidth, renderer.textureHeight);
        this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, faces[5].u0, faces[5].v0, faces[5].u1, faces[5].v1, renderer.textureWidth, renderer.textureHeight);

        if (renderer.mirror)
        {
            for (int j1 = 0; j1 < this.quadList.length; ++j1)
            {
                this.quadList[j1].flipFace();
            }
        }
	}
	
	public MutatedBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation, boolean mirrored, byte faceVisibilityFlag)
    {
		super(modelRenderer, texU, texV, x, y, z, (int) width, (int) height, (int) length, inflation);
		this.faceVisibilityFlag = faceVisibilityFlag;
        float f4 = x + (float)width;
        float f5 = y + (float)height;
        float f6 = z + (float)length;
        x -= inflation;
        y -= inflation;
        z -= inflation;
        f4 += inflation;
        f5 += inflation;
        f6 += inflation;
        
        if (mirrored)
        {
            float f7 = f4;
            f4 = x;
            x = f7;
        }
        
        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f4, y, z, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f5, z, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f5, z, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f4, y, f6, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f5, f6, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f5, f6, 8.0F, 0.0F);
        this.vertexPositions[0] = positiontexturevertex7;
        this.vertexPositions[1] = positiontexturevertex;
        this.vertexPositions[2] = positiontexturevertex1;
        this.vertexPositions[3] = positiontexturevertex2;
        this.vertexPositions[4] = positiontexturevertex3;
        this.vertexPositions[5] = positiontexturevertex4;
        this.vertexPositions[6] = positiontexturevertex5;
        this.vertexPositions[7] = positiontexturevertex6;
        this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + length, texU + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + length, texV, texU + length + width, texV + length, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + width, texV, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + length, texV + length, texU + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + length + width + length, texV + length, texU + length + width + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);

        if (mirrored)
        {
            for (int j1 = 0; j1 < this.quadList.length; ++j1)
            {
                this.quadList[j1].flipFace();
            }
        }
    }
	
	public MutatedBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation, boolean mirrored)
	{
		this(modelRenderer, texU, texV, x, y, z, width, height, length, inflation, mirrored, (byte) 0b111111);
	}
	
	public MutatedBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation)
	{
		this(modelRenderer, texU, texV, x, y, z, width, height, length, inflation, modelRenderer.mirror, (byte) 0b111111);
	}
	
	@Override
    public void render(BufferBuilder p_178780_1_, float p_78245_2_)
    {
        for (int i = 0; i < this.quadList.length; ++i)
        {
        	// This check is done to not draw hidden
        	// faces.
        	if (this.isFaceVisible(i))
        		this.quadList[i].draw(p_178780_1_, p_78245_2_);
        }
    }
	
	public boolean isFaceVisible(int faceIndex)
	{
		return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
	}
	
}
