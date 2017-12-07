package net.gobbob.mobends.client.model;

import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelPartChild extends ModelPart
{
	protected IModelPart parent;
	protected boolean hideLikeParent = false;
	
	public ModelPartChild(ModelBase model, boolean register, int texOffsetX, int texOffsetY) {
		super(model, register, texOffsetX, texOffsetY);
	}
	
	public ModelPartChild(ModelBase model, boolean register)
	{
		this(model, register, 0, 0);
	}
	
	public ModelPartChild(ModelBase model, int texOffsetX, int texOffsetY)
    {
		this(model, true, texOffsetX, texOffsetY);
    }
    
	public ModelPartChild setParent(IModelPart parent)
	{
		this.parent = parent;
		return this;
	}
	
	public ModelPartChild setHideLikeParent(boolean flag){
		this.hideLikeParent = flag;
		return this;
	}
	
	@Override
    public void applyStandaloneTransform(float scale)
    {
		if(this.parent != null)
    		this.parent.applyStandaloneTransform(scale);
		
		super.applyStandaloneTransform(scale);
    }
}
