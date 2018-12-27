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
	public void renderPart(float scale)
	{
		if (!(this.isShowing())) return;
        if (!this.compiled)
            this.compileDisplayList(scale);
        
        GlStateManager.pushMatrix();
        
        this.applyStandaloneTransform(scale);
        GlStateManager.callList(this.displayList);
        if(extension != null)
        	extension.renderJustPart(scale);
        
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
        if(extension != null)
        	extension.renderJustPart(scale);
        
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
	public void applyPostTransform(float scale)
	{
		if(extension != null)
			extension.propagateTransform(scale);
	}
	
	@Override
	public void propagateTransform(float scale)
	{
		super.propagateTransform(scale);
		this.applyPostTransform(scale);
	}
}
