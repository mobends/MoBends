package net.gobbob.mobends.standard.client.renderer.entity;

import net.gobbob.mobends.standard.main.MoBends;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BendsCapeRenderer {
	public static final int MODEL_WIDTH = 10;
	public static final int MODEL_LENGTH = 16;
	public static final int MODEL_DEPTH = 1;
	public static final int SLAB_AMOUNT = 16;
	
	public static final ResourceLocation CAPE_TEXTURE = new ResourceLocation(ModStatics.MODID,"textures/cape.png");
	
	public Slab[] slabs;
	
	public BendsCapeRenderer() {
		this.slabs = new Slab[SLAB_AMOUNT];
		for(int i = 0; i < SLAB_AMOUNT; i++) {
			this.slabs[i] = new Slab(i*MODEL_LENGTH/SLAB_AMOUNT);
			if(i > 0)
				this.slabs[i-1].setChildSlab(this.slabs[i]);
		}
		this.slabs[0].rotationPointY = 0;
	}
	
	public void animate(AbstractClientPlayer player, float partialTicks, float ageInTicks) {
		for(int i = 0; i < SLAB_AMOUNT; i++) {
			float waveSpeed = 0.2f;
			float waveFrequency = 7.2f;
			float waveOffset = ((float)i)/SLAB_AMOUNT;
			float magnitude = (80.0f/SLAB_AMOUNT*(0.7f+i/SLAB_AMOUNT));
			this.slabs[i].setRotateAngle((float) (Math.cos((ageInTicks)*waveSpeed + waveOffset*waveFrequency)*magnitude));
		}
		this.slabs[0].rotate(-10.0f);
    }
	
	public void render(float scale) {
		this.slabs[0].render(scale);
	}
	
	static class Slab {
		float rotateAngle;
		
		public float textureWidth = 64;
	    public float textureHeight = 32;
	    private int textureOffsetX;
	    private int textureOffsetY;
	    private boolean compiled;
	    /** The GL display list rendered by the Tessellator for this model */
	    private int displayList;
	    private Slab childSlab;
	    public boolean showModel;
	    /** Hides the model. */
	    public boolean isHidden;
	    public int offsetX;
	    public int offsetY;
	    public int offsetZ;
	    public int rotationPointX;
	    public int rotationPointY;
	    public int rotationPointZ;
	    public int hingeOffset = 0;
	    
	    private final PositionTextureVertex[] vertexPositions;
	    private final TexturedQuad[] quadList;
	    /** X vertex coordinate of lower box corner */
	    public final float posX1;
	    /** Y vertex coordinate of lower box corner */
	    public final float posY1;
	    /** Z vertex coordinate of lower box corner */
	    public final float posZ1;
	    /** X vertex coordinate of upper box corner */
	    public final float posX2;
	    /** Y vertex coordinate of upper box corner */
	    public final float posY2;
	    /** Z vertex coordinate of upper box corner */
	    public final float posZ2;
	    
	    public Slab(int texV)
	    {
	        this.textureWidth = 64.0F;
	        this.textureHeight = 32.0F;
	        this.showModel = true;
	        this.rotationPointX = 0;
	        this.rotationPointY = MODEL_LENGTH/SLAB_AMOUNT;
	        this.rotationPointZ = 0;
	        this.offsetX = -MODEL_WIDTH/2;
	        this.offsetY = 0;
	        this.offsetZ = 0;
	        int slabLength = MODEL_LENGTH / SLAB_AMOUNT;
	        this.posX1 = this.offsetX;
	        this.posY1 = this.offsetY;
	        this.posZ1 = this.offsetZ;
	        this.posX2 = this.offsetX + MODEL_WIDTH;
	        this.posY2 = this.offsetY + slabLength;
	        this.posZ2 = this.offsetZ + MODEL_DEPTH;
	        int texU = 0;
	        
	        this.vertexPositions = new PositionTextureVertex[8];
	        this.quadList = new TexturedQuad[6];

	        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(posX1, posY1, posZ1, 0.0F, 0.0F);
	        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(posX2, posY1, posZ1, 0.0F, 8.0F);
	        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(posX2, posY2, posZ1, 8.0F, 8.0F);
	        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(posX1, posY2, posZ1, 8.0F, 0.0F);
	        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(posX1, posY1, posZ2, 0.0F, 0.0F);
	        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(posX2, posY1, posZ2, 0.0F, 8.0F);
	        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(posX2, posY2, posZ2, 8.0F, 8.0F);
	        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(posX1, posY2, posZ2, 8.0F, 0.0F);
	        this.vertexPositions[0] = positiontexturevertex7;
	        this.vertexPositions[1] = positiontexturevertex;
	        this.vertexPositions[2] = positiontexturevertex1;
	        this.vertexPositions[3] = positiontexturevertex2;
	        this.vertexPositions[4] = positiontexturevertex3;
	        this.vertexPositions[5] = positiontexturevertex4;
	        this.vertexPositions[6] = positiontexturevertex5;
	        this.vertexPositions[7] = positiontexturevertex6;
	        this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH, texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH, texV + MODEL_DEPTH + slabLength, textureWidth, textureHeight);
	        this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + MODEL_DEPTH, texU + MODEL_DEPTH, texV + MODEL_DEPTH + slabLength, textureWidth, textureHeight);
	        this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + MODEL_DEPTH, texV, texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH, textureWidth, textureHeight);
	        this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH, texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_WIDTH, texV, textureWidth, textureHeight);
	        this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + MODEL_DEPTH, texV + MODEL_DEPTH, texU + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH + slabLength, textureWidth, textureHeight);
	        this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH, texV + MODEL_DEPTH, texU + MODEL_DEPTH + MODEL_WIDTH + MODEL_DEPTH + MODEL_WIDTH, texV + MODEL_DEPTH + slabLength, textureWidth, textureHeight);
	    }

	    public void rotate(float f) {
			this.setRotateAngle(this.rotateAngle+f);
		}

		public Slab setChildSlab(Slab slab) {
	    	this.childSlab = slab;
	    	return this;
	    }
	    
	    public void setRotateAngle(float rotateAngle) {
        	this.rotateAngle = rotateAngle;
    		this.hingeOffset = this.rotateAngle < 0 ? MODEL_DEPTH : 0;
        }
	    
	    @SideOnly(Side.CLIENT)
	    public void render(float scale)
	    {
	        if (!this.isHidden)
	        {
	            if (this.showModel)
	            {
	                if (!this.compiled)
	                {
	                    this.compileDisplayList(scale);
	                }

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, (this.rotationPointZ + this.hingeOffset) * scale);
                    GlStateManager.rotate(this.rotateAngle, 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(0, 0, -this.hingeOffset * scale);
                    
                    GlStateManager.callList(this.displayList);
                    if(this.childSlab != null)
                    	this.childSlab.render(scale);

                    GlStateManager.popMatrix();
	            }
	        }
	    }
	    
	    private void compileDisplayList(float scale)
	    {
	        this.displayList = GLAllocation.generateDisplayLists(1);
	        GlStateManager.glNewList(this.displayList, 4864);
	        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

            for (TexturedQuad texturedquad : this.quadList)
            {
                texturedquad.draw(bufferbuilder, scale);
            }

	        GlStateManager.glEndList();
	        this.compiled = true;
	    }
	}
}
