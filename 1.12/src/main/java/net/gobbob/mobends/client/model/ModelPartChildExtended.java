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

public class ModelPartChildExtended extends ModelPartChild{
	protected IModelPart extension;
	
	public ModelPartChildExtended(ModelBase model, boolean register, int texOffsetX, int texOffsetY) {
		super(model, register, texOffsetX, texOffsetY);
	}
	
	public ModelPartChildExtended(ModelBase model, boolean register)
	{
		this(model, register, 0, 0);
	}
	
	public ModelPartChildExtended(ModelBase model, int texOffsetX, int texOffsetY)
    {
		this(model, true, texOffsetX, texOffsetY);
    }
	
	public ModelPartChildExtended setExtension(IModelPart modelPart) {
		extension = modelPart;
		return this;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void render(float scale)
    {
        if (this.isHidden || !this.showModel) return;
        if (!this.compiled)
            this.compileDisplayList(scale);
        
        GlStateManager.pushMatrix();
	        super.postRender(scale);
	        
	        GlStateManager.callList(this.displayList);
	        
	        if (this.childModels != null)
	        {
	            for (int k = 0; k < this.childModels.size(); ++k)
	            {
	                ((ModelRenderer)this.childModels.get(k)).render(scale);
	            }
	        }
	        
	        if(extension != null)
	        	extension.renderPart(scale);
        GlStateManager.popMatrix();
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void postRender(float scale)
    {
		super.postRender(scale);
		
		if(extension != null)
        	extension.applyTransform(scale);
    }
}
