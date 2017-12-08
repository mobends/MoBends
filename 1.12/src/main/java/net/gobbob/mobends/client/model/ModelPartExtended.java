package net.gobbob.mobends.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelPartExtended extends ModelPart {
	protected IModelPart extension;
	
	public ModelPartExtended(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, register, texOffsetX, texOffsetY);
	}

	public ModelPartExtended(ModelBase model, boolean register)
	{
		super(model, register);
	}

	public ModelPartExtended(ModelBase model, int texOffsetX, int texOffsetY)
	{
		super(model, texOffsetX, texOffsetY);
	}

	public ModelPartExtended setExtension(IModelPart modelPart) {
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
	public void applyStandaloneTransform(float scale) {
		super.applyStandaloneTransform(scale);
	}
	
	@Override
	public void applyPostTransform(float scale)
	{
		if(extension != null)
			extension.propagateTransform(scale);
	}
	
	@Override
	public void propagateTransform(float scale) {
		super.propagateTransform(scale);
		this.applyPostTransform(scale);
	}
}
