package net.gobbob.mobends.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexBuffer;

public class ModelBox extends net.minecraft.client.model.ModelBox{
	
	public float x, y, z;
	public float width, height, length;
	public int originalWidth, originalHeight, originalLength;
	public float texU, texV;
	
	public float inflation = 0.0f;
	
	public PositionTextureVertex[] vertices;
	/*0-LEFT, 1-RIGHT, 2-TOP, 3-BOTTOM, 4-FRONT, 5-BACK*/
	public TexturedQuad[] quads;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	public static final int FRONT = 4;
	public static final int BACK = 5;
	
	public ModelBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation)
    {
		super(modelRenderer, texU, texV, x, y, z, (int) width, (int) height, (int) length, inflation);
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = this.originalWidth = width;
        this.height = this.originalHeight = height;
        this.length = this.originalLength = length;
        this.texU = texU;
        this.texV = texV;
        this.vertices = new PositionTextureVertex[8];
        this.quads = new TexturedQuad[6];
        float f4 = x + (float)width;
        float f5 = y + (float)height;
        float f6 = z + (float)length;
        x -= inflation;
        y -= inflation;
        z -= inflation;
        f4 += inflation;
        f5 += inflation;
        f6 += inflation;
        
        this.inflation = inflation;
        
        if (modelRenderer.mirror)
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
        this.vertices[0] = positiontexturevertex7;
        this.vertices[1] = positiontexturevertex;
        this.vertices[2] = positiontexturevertex1;
        this.vertices[3] = positiontexturevertex2;
        this.vertices[4] = positiontexturevertex3;
        this.vertices[5] = positiontexturevertex4;
        this.vertices[6] = positiontexturevertex5;
        this.vertices[7] = positiontexturevertex6;
        this.quads[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + length, texU + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + length, texV, texU + length + width, texV + length, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + width, texV, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + length, texV + length, texU + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + length + width + length, texV + length, texU + length + width + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);

        if (modelRenderer.mirror)
        {
            for (int j1 = 0; j1 < this.quads.length; ++j1)
            {
                this.quads[j1].flipFace();
            }
        }
    }
	
	public void updateVertices(ModelRenderer modelRenderer)
	{
		float f4 = this.x + this.width;
        float f5 = this.y + this.height;
        float f6 = this.z + this.length;
        
        int p_i1171_2_ = (int) this.texU;
        int p_i1171_3_ = (int) this.texV;
        
        int p_i1171_7_ = this.originalWidth;
        int p_i1171_8_ = this.originalHeight;
        int p_i1171_9_ = this.originalLength;
        
        float p_x = this.x;
        float p_y = this.y;
        float p_z = this.z;
        
        p_x -= this.inflation;
        p_y -= this.inflation;
        p_z -= this.inflation;
        f4 += this.inflation;
        f5 += this.inflation;
        f6 += this.inflation;
        
        if (modelRenderer.mirror)
        {
            float f7 = f4;
            f4 = p_x;
            p_x = f7;
        }
        
		PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(p_x, p_y, p_z, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f4, p_y, p_z, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f5, p_z, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(p_x, f5, p_z, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(p_x, p_y, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f4, p_y, f6, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f5, f6, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(p_x, f5, f6, 8.0F, 0.0F);
        this.vertices[0] = positiontexturevertex7;
        this.vertices[1] = positiontexturevertex;
        this.vertices[2] = positiontexturevertex1;
        this.vertices[3] = positiontexturevertex2;
        this.vertices[4] = positiontexturevertex3;
        this.vertices[5] = positiontexturevertex4;
        this.vertices[6] = positiontexturevertex5;
        this.vertices[7] = positiontexturevertex6;
        this.quads[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, p_i1171_2_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, p_i1171_2_ + p_i1171_9_, p_i1171_3_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_7_, p_i1171_3_, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, modelRenderer.textureWidth, modelRenderer.textureHeight);

        if (modelRenderer.mirror)
        {
            for (int j1 = 0; j1 < this.quads.length; ++j1)
            {
                this.quads[j1].flipFace();
            }
            
            p_x = f4;
        }
	}
	
	@Override
    public void render(BufferBuilder p_178780_1_, float p_78245_2_)
    {
        for (int i = 0; i < this.quads.length; ++i)
        {
            this.quads[i].draw(p_178780_1_, p_78245_2_);
        }
    }
	
	public ModelBox offsetTextureQuad(ModelRenderer modelPart, int idx, float x, float y){
		if(idx >= 0 & idx < this.quads.length){
			this.quads[idx].vertexPositions[0].texturePositionX += x/modelPart.textureWidth;
			this.quads[idx].vertexPositions[1].texturePositionX += x/modelPart.textureWidth;
			this.quads[idx].vertexPositions[2].texturePositionX += x/modelPart.textureWidth;
			this.quads[idx].vertexPositions[3].texturePositionX += x/modelPart.textureWidth;
			
			this.quads[idx].vertexPositions[0].texturePositionY += y/modelPart.textureHeight;
			this.quads[idx].vertexPositions[1].texturePositionY += y/modelPart.textureHeight;
			this.quads[idx].vertexPositions[2].texturePositionY += y/modelPart.textureHeight;
			this.quads[idx].vertexPositions[3].texturePositionY += y/modelPart.textureHeight;
		}
		return this;
	}
	
	public void resize(float width, float height, float length){
		this.width = width;
		this.height = height;
		this.length = length;
	}
	
	public void setPosition(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void offset(float x, float y, float z){
		this.x += x;
		this.y += y;
		this.z += z;
	}
}
