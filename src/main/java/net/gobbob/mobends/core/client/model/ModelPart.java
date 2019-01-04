package net.gobbob.mobends.core.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.util.GLHelper;
import net.gobbob.mobends.core.util.SmoothOrientation;
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
	public Vector3f position;
	public Vector3f scale;
	public SmoothOrientation rotation;
	public int texOffsetX, texOffsetY;
	
	public boolean compiled;
	protected int displayList;
	
	public ModelPart(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, texOffsetY, texOffsetY);
		this.position = new Vector3f();
		this.scale = new Vector3f(1, 1, 1);
		this.rotation = new SmoothOrientation();
		this.texOffsetX = texOffsetX;
        this.texOffsetY = texOffsetY;
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
        this.renderPart(scale);
    }
	
	@Override
	public void renderPart(float scale)
	{
		if (!(this.isShowing())) return;
        if (!this.compiled)
            this.compileDisplayList(scale);
        
        GlStateManager.pushMatrix();
        
        this.applyStandaloneTransform(scale);
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
	public void renderJustPart(float scale)
	{
		if (!(this.isShowing())) return;
        if (!this.compiled)
            this.compileDisplayList(scale);
        
        GlStateManager.pushMatrix();
        
        this.applyOwnTransform(scale);
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

        GLHelper.rotate(rotation.getSmooth());

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
        this.applyStandaloneTransform(scale);
        this.applyPostTransform(scale);
    }
	
	@Override
	public void applyStandaloneTransform(float scale)
	{
		this.applyOwnTransform(scale);
	}
	
	@Override
	public void applyOwnTransform(float scale)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
        	GlStateManager.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
        
		GLHelper.rotate(rotation.getSmooth());
        
        if(this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
        	GlStateManager.scale(this.scale.x, this.scale.y, this.scale.z);
	}
	
	@Override
	public void propagateTransform(float scale)
	{
		this.applyOwnTransform(scale);
	}
	
	@Override
	public void applyPostTransform(float scale)
	{
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
	
    @Override
	public void update(float ticksPerFrame)
	{
		this.rotation.update(ticksPerFrame);
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
		this.scale.x = x;
		this.scale.y = y;
		this.scale.z = z;
		return this;
	}
	
	public ModelPart resetScale()
	{
		this.scale.set(0, 0, 0);
		return this;
	}
	
	public ModelPart setBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
    {
        this.addBox(x, y, z, width, height, length, scaleFactor);
        return this;
    }
	
	public ModelPart setBox(float x, float y, float z, int width, int height, int length)
    {
        return this.setBox(x, y, z, width, height, length, 0.0F);
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
	
	@Override
	public Vector3f getPosition() { return this.position; }
	@Override
	public Vector3f getScale() { return this.scale; }
	@Override
	public SmoothOrientation getRotation() { return this.rotation; }
	@Override
	public boolean isShowing() { return this.showModel && !this.isHidden; }
	
	public ModelPart setBoxPosition(float x, float y, float z)
	{
		this.getBox().setPosition(x, y, z);
		return this;
	}
	
	public ModelPart offsetBoxBy(float x, float y, float z)
	{
		this.getBox().offset(x, y, z);
		return this;
	}
	
	public ModelPart resizeBox(float width, float height, float length)
	{
		this.getBox().resize(width, height, length);
		return this;
	}
	
	public ModelPart setWidth(float width) {
		this.getBox().width = width;
		return this;
	}
	
	public ModelPart setHeight(float height) {
		this.getBox().height = height;
		return this;
	}
	
	public ModelPart setLength(float length) {
		this.getBox().length = length;
		return this;
	}
	
	public ModelPart updateVertices()
	{
		this.getBox().updateVertices(this);
		this.compiled = false;
		return this;
	}
	
	public ModelPart setTextureOffset(int x, int y)
    {
        this.texOffsetX = x;
        this.texOffsetY = y;
        return this;
    }
	
	public ModelPart setVisibility(int faceIndex, boolean visible)
	{
		this.getBox().setVisibility(faceIndex, visible);
		return this;
	}
	
	public ModelPart setMirror(boolean mirror)
	{
		this.mirror = mirror;
		return this;
	}
	
	public void finish()
	{
		this.rotation.finish();
	}
	
	@Override
	public void syncUp(IModelPart part)
	{
		if(part == null)
			return;
		this.position.set(part.getPosition());
		this.rotation.set(part.getRotation());
		this.scale.set(part.getScale());
	}

	@Override
	public void setVisible(boolean showModel)
	{
		this.showModel = showModel;
	}
}
