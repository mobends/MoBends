package net.gobbob.mobends.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexBuffer;

public class ModelBox extends net.minecraft.client.model.ModelBox{
	
	public float offsetX,offsetY,offsetZ;
	public float resX,resY,resZ;
	public float originalResX, originalResY, originalResZ;
	
	public float txOffsetX, txOffsetY;
	
	public float addSize = 0.0f;
	
	public PositionTextureVertex[] vertices;
	/*0-LEFT, 1-RIGHT, 2-TOP, 3-BOTTOM, 4-FRONT, 5-BACK*/
	public TexturedQuad[] quads;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	public static final int FRONT = 4;
	public static final int BACK = 5;
	
	public ModelBox(ModelRenderer modelRenderer, int texOffsetX, int texOffsetY, float offsetX, float offsetY, float offsetZ, int width, int height, int length, float p_i1171_10_)
    {
		super(modelRenderer, texOffsetX, texOffsetY, p_i1171_10_, p_i1171_10_, p_i1171_10_, length, length, length, p_i1171_10_);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.resX = this.originalResX = (float)width;
        this.resY = this.originalResY = (float)height;
        this.resZ = this.originalResZ = (float)length;
        this.txOffsetX = texOffsetX;
        this.txOffsetY = texOffsetY;
        this.vertices = new PositionTextureVertex[8];
        this.quads = new TexturedQuad[6];
        float f4 = offsetX + (float)width;
        float f5 = offsetY + (float)height;
        float f6 = offsetZ + (float)length;
        offsetX -= p_i1171_10_;
        offsetY -= p_i1171_10_;
        offsetZ -= p_i1171_10_;
        f4 += p_i1171_10_;
        f5 += p_i1171_10_;
        f6 += p_i1171_10_;
        
        this.addSize = p_i1171_10_;
        
        if (modelRenderer.mirror)
        {
            float f7 = f4;
            f4 = offsetX;
            offsetX = f7;
        }

        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(offsetX, offsetY, offsetZ, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f4, offsetY, offsetZ, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f5, offsetZ, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(offsetX, f5, offsetZ, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(offsetX, offsetY, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f4, offsetY, f6, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f5, f6, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(offsetX, f5, f6, 8.0F, 0.0F);
        this.vertices[0] = positiontexturevertex7;
        this.vertices[1] = positiontexturevertex;
        this.vertices[2] = positiontexturevertex1;
        this.vertices[3] = positiontexturevertex2;
        this.vertices[4] = positiontexturevertex3;
        this.vertices[5] = positiontexturevertex4;
        this.vertices[6] = positiontexturevertex5;
        this.vertices[7] = positiontexturevertex6;
        this.quads[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texOffsetX + length + width, texOffsetY + length, texOffsetX + length + width + length, texOffsetY + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texOffsetX, texOffsetY + length, texOffsetX + length, texOffsetY + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texOffsetX + length, texOffsetY, texOffsetX + length + width, texOffsetY + length, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texOffsetX + length + width, texOffsetY + length, texOffsetX + length + width + width, texOffsetY, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texOffsetX + length, texOffsetY + length, texOffsetX + length + width, texOffsetY + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.quads[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texOffsetX + length + width + length, texOffsetY + length, texOffsetX + length + width + length + width, texOffsetY + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight);

        if (modelRenderer.mirror)
        {
            for (int j1 = 0; j1 < this.quads.length; ++j1)
            {
                this.quads[j1].flipFace();
            }
        }
    }
	
	public void updateVertexPositions(ModelRenderer p_i1171_1_){
		float f4 = this.offsetX + this.resX;
        float f5 = this.offsetY + this.resY;
        float f6 = this.offsetZ + this.resZ;
        
        int p_i1171_2_ = (int) this.txOffsetX;
        int p_i1171_3_ = (int) this.txOffsetY;
        
        int p_i1171_7_ = (int) this.originalResX;
        int p_i1171_8_ = (int) this.originalResY;
        int p_i1171_9_ = (int) this.originalResZ;
        
        float p_x = this.offsetX;
        float p_y = this.offsetY;
        float p_z = this.offsetZ;
        
        p_x -= this.addSize;
        p_y -= this.addSize;
        p_z -= this.addSize;
        f4 += this.addSize;
        f5 += this.addSize;
        f6 += this.addSize;
        
        if (p_i1171_1_.mirror)
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
        this.quads[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
        this.quads[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, p_i1171_2_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
        this.quads[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, p_i1171_2_ + p_i1171_9_, p_i1171_3_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
        this.quads[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_7_, p_i1171_3_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
        this.quads[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, p_i1171_2_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);
        this.quads[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_, p_i1171_3_ + p_i1171_9_, p_i1171_2_ + p_i1171_9_ + p_i1171_7_ + p_i1171_9_ + p_i1171_7_, p_i1171_3_ + p_i1171_9_ + p_i1171_8_, p_i1171_1_.textureWidth, p_i1171_1_.textureHeight);

        if (p_i1171_1_.mirror)
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
	
	public void resize(float x, float y, float z){
		this.resX = x;
		this.resY = y;
		this.resZ = z;
	}
	
	public void offset(float x, float y, float z){
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
	}
	
	public void offset_add(float x, float y, float z){
		this.offsetX += x;
		this.offsetY += y;
		this.offsetZ += z;
	}
}
