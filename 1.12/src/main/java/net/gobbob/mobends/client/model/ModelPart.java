package net.gobbob.mobends.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelPart extends ModelRenderer implements IModelPart
{
	public Vector3f position = new Vector3f();
	public SmoothVector3f rotation = new SmoothVector3f();
	public SmoothVector3f pre_rotation = new SmoothVector3f();
	public float scaleX, scaleY, scaleZ;
	public int texOffsetX, texOffsetY;
	
	public boolean compiled;
	protected int displayList;
	
	public ModelPart(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, texOffsetY, texOffsetY);
		this.texOffsetX = texOffsetX;
        this.texOffsetY = texOffsetY;
        this.scaleX = this.scaleY = this.scaleZ = 1.0f;
        if(!register)
        	model.boxList.remove(model.boxList.size() - 1);
	}
	
	public ModelPart(ModelBase model, boolean register)
	{
		this(model, register, 0, 0);
	}
	
	public ModelPart(ModelBase model, int texOffsetX, int texOffsetY)
    {
        this(model, true, texOffsetX, texOffsetY);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void render(float scale)
    {
        if (this.isHidden || !this.showModel) return;
        if (!this.compiled)
            this.compileDisplayList(scale);
        
        GlStateManager.pushMatrix();
	        postRender(scale);
	        
	        GlStateManager.callList(this.displayList);
	        
	        if (this.childModels != null)
	        {
	            for (int k = 0; k < this.childModels.size(); ++k)
	            {
	                ((ModelRenderer)this.childModels.get(k)).render(scale);
	            }
	        }
        GlStateManager.popMatrix();
    }

	@Override
    @SideOnly(Side.CLIENT)
    public void renderWithRotation(float scale)
    {
        if (this.isHidden || !this.showModel) return;
        if (!this.compiled)
            this.compileDisplayList(scale);

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);

        if (this.rotation.getZ() != 0.0F)
            GlStateManager.rotate(this.rotation.getZ(), 0F, 0F, 1F);
        if (this.rotation.getY() != 0.0F)
            GlStateManager.rotate(this.rotation.getY(), 0F, 1F, 0F);
        if (this.rotation.getX() != 0.0F)
            GlStateManager.rotate(this.rotation.getX(), 1F, 0F, 0F);

        GlStateManager.callList(this.displayList);
        GlStateManager.popMatrix();
    }

    /**
     * Allows the changing of Angles after a box has been rendered
     */
	@Override
    @SideOnly(Side.CLIENT)
    public void postRender(float scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
        	GlStateManager.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
        
        if (this.rotation.getZ() != 0.0F)
            GlStateManager.rotate(this.rotation.getZ(), 0F, 0F, 1F);
        if (this.rotation.getY() != 0.0F)
            GlStateManager.rotate(this.rotation.getY(), 0F, 1F, 0F);
        if (this.rotation.getX() != 0.0F)
            GlStateManager.rotate(this.rotation.getX(), 1F, 0F, 0F);
    }

    /**
     * Compiles a GL display list for this model
     */
    @SideOnly(Side.CLIENT)
    protected void compileDisplayList(float scale)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        for (int i = 0; i < this.cubeList.size(); ++i)
        {
            ((ModelBox)this.cubeList.get(i)).render(bufferbuilder, scale);
        }

        GlStateManager.glEndList();
        this.compiled = true;
    }
	
	public void update(float p_78785_1_)
	{
		this.rotation.update(p_78785_1_);
		this.pre_rotation.update(p_78785_1_);
	}
	
	@Override
	public void setRotationPoint(float x, float y, float z)
    {
        this.position.set(x, y, z);
    }
	
	public ModelPart setPosition(float x, float y, float z)
	{
		this.setRotationPoint(x, y, z);
		return this;
	}
	
	public ModelPart setOffset(float x, float y, float z)
	{
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
		return this;
	}
	
	public ModelPart setScale(float x, float y, float z)
	{
		this.scaleX = x;
		this.scaleY = y;
		this.scaleZ = z;
		return this;
	}
	
	public ModelPart resetScale()
	{
		this.scaleX = this.scaleY = this.scaleZ = 1.0f;
		return this;
	}
	
	public void sync(ModelPart box)
	{
		if(box != null)
		{
			this.rotateAngleX = box.rotateAngleX;
			this.rotateAngleY = box.rotateAngleY;
			this.rotateAngleZ = box.rotateAngleZ;
			this.rotation.vOld.set(box.rotation.vOld);
			this.rotation.completion.set(box.rotation.completion);
			this.rotation.vFinal.set(box.rotation.vFinal);
			this.rotation.vSmooth.set(box.rotation.vSmooth);
			this.rotation.smoothness.set(box.rotation.smoothness);

			this.pre_rotation.vOld.set(box.pre_rotation.vOld);
			this.pre_rotation.completion.set(box.pre_rotation.completion);
			this.pre_rotation.vFinal.set(box.pre_rotation.vFinal);
			this.pre_rotation.vSmooth.set(box.pre_rotation.vSmooth);
			this.pre_rotation.smoothness.set(box.pre_rotation.smoothness);
			
			this.scaleX = box.scaleX;
			this.scaleY = box.scaleY;
			this.scaleZ = box.scaleZ;
		}
	}
	
	public void addBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
    {
        this.addModelBox(x, y, z, width, height, length, scaleFactor);
    }
	
	public ModelBox addModelBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
	{
		ModelBox box = new ModelBox(this, this.texOffsetX, this.texOffsetY, x, y, z, width, height, length, scaleFactor);
		this.cubeList.add(box);
		this.compiled = false;
		return box;
	}
	
	public ModelBox getBox()
	{
		return getBox(0);
	}
	
	public ModelBox getBox(int idx)
	{
		return ((ModelBox)this.cubeList.get(idx));
	}
	
	public ModelPart offsetBox(float x, float y, float z)
	{
		this.getBox().offset(x, y, z);
		return this;
	}
	
	public ModelPart offsetBox_Add(float x, float y, float z)
	{
		this.getBox().offset_add(x, y, z);
		return this;
	}
	
	public ModelPart resizeBox(float x, float y, float z)
	{
		this.getBox().resize(x, y, z);
		return this;
	}
	
	public ModelPart updateVertices()
	{
		this.getBox().updateVertexPositions(this);
		this.compiled = false;
		return this;
	}
	
	public ModelPart setTextureOffset(int x, int y)
    {
        this.texOffsetX = x;
        this.texOffsetY = y;
        return this;
    }
	
	public void finish()
	{
		this.rotation.finish();
		this.pre_rotation.finish();
	}

	@Override
	public void applyTransform(float scale)
	{
		this.postRender(scale);
	}

	@Override
	public void renderPart(float scale)
	{
		this.render(scale);
	}
}
