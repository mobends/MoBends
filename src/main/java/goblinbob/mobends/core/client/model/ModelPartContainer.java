package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.TransformUtils;
import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.math.physics.ICollider;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelPartContainer extends ModelRenderer implements IModelPart
{
	
	public Vec3f position;
	/**
	 * A secondary position variable is used to offset
	 * the model relative to the first position, which may
	 * be overridden by animation.
	 * */
	public Vec3f offset;
	/**
	 * Used to offset the container item relative to
	 * the rotation point
	 * */
	public Vec3f innerOffset;
	public Vec3f scale;
	public SmoothOrientation rotation;
	
	private ModelRenderer model;
	
	protected IModelPart parent;
	protected ICollider collider;
	
	public ModelPartContainer(ModelBase modelBase, ModelRenderer model)
	{
		super(modelBase, 0, 0);
		this.model = model;
		this.position = new Vec3f();
		this.offset = new Vec3f();
		this.innerOffset = new Vec3f();
		this.scale = new Vec3f(1, 1, 1);
		this.rotation = new SmoothOrientation();
	}

	public ModelRenderer getModel() { return this.model; }
	public IModelPart getParent() { return this.parent; }
	
	public ModelPartContainer setParent(IModelPart parent)
	{
		this.parent = parent;
		return this;
	}
	
	public ModelPartContainer setPosition(float x, float y, float z)
	{
		this.position.set(x, y, z);
		return this;
	}
	
	public ModelPartContainer setOffset(float x, float y, float z)
	{
		this.offset.set(x, y, z);
		return this;
	}
	
	public ModelPartContainer setInnerOffset(float x, float y, float z)
	{
		this.innerOffset.set(x, y, z);
		return this;
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
		
        GlStateManager.pushMatrix();
        this.applyCharacterTransform(scale);
        this.model.rotateAngleX = this.model.rotateAngleY = this.model.rotateAngleZ = 0;
        
        // This is applied outside the standalone transform method, so that children aren't affected.
        if (this.innerOffset.x != 0.0F || this.innerOffset.y != 0.0F || this.innerOffset.z != 0.0F)
        	GlStateManager.translate(this.innerOffset.x * scale, this.innerOffset.y * scale, this.innerOffset.z * scale);
        
        this.model.render(scale);
        
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
		
        GlStateManager.pushMatrix();
        this.applyLocalTransform(scale);
        
        // This is applied outside the standalone transform method, so that children aren't affected.
        if (this.innerOffset.x != 0.0F || this.innerOffset.y != 0.0F || this.innerOffset.z != 0.0F)
        	GlStateManager.translate(this.innerOffset.x * scale, this.innerOffset.y * scale, this.innerOffset.z * scale);
        
        this.model.render(scale);
        
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
    public void postRender(float scale)
    {
        this.applyCharacterTransform(scale);
        this.applyPostTransform(scale);
    }
	
	@Override
	public void applyCharacterTransform(float scale)
	{
		if (this.parent != null)
			this.parent.applyCharacterTransform(scale);
		this.applyLocalTransform(scale);
	}

	@Override
	public void applyLocalTransform(float scale)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
        	GlStateManager.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
		if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
        	GlStateManager.translate(this.offset.x * scale, this.offset.y * scale, this.offset.z * scale);
		
		GlHelper.rotate(this.rotation.getSmooth());
        
        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
        	GlStateManager.scale(this.scale.x, this.scale.y, this.scale.z);
	}

	@Override
	public void applyPostTransform(float scale)
	{
	}

	@Override
	public void update(float ticksPerFrame)
	{
		this.rotation.update(ticksPerFrame);
	}

	@Override
	public Vec3f getPosition() { return this.position; }
	@Override
	public Vec3f getScale() { return this.scale; }
	@Override
	public SmoothOrientation getRotation() { return this.rotation; }

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
	public boolean isShowing()
	{
		return this.showModel && !this.isHidden;
	}

	@Override
	public void setVisible(boolean showModel)
	{
		this.showModel = showModel;
	}

	@Override
	public void applyLocalTransform(float scale, IMat4x4d matrix)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
			TransformUtils.translate(matrix, this.position.x * scale, this.position.y * scale, this.position.z * scale, matrix);
		if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
			TransformUtils.translate(matrix, this.offset.x * scale, this.offset.y * scale, this.offset.z * scale);
		
    	TransformUtils.rotate(matrix, rotation.getSmooth());
    	
    	if(this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
    		TransformUtils.scale(matrix, this.scale.x, this.scale.y, this.scale.z);
	}
	
}
