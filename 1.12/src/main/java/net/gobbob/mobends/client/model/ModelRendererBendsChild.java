package net.gobbob.mobends.client.model;

import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelRendererBendsChild extends ModelRendererBends{
	protected ModelRendererBends parent;
	
	public ModelRendererBendsChild(ModelBase model, boolean register)
	{
		super(model, register);
	}
	
	public ModelRendererBendsChild(ModelBase model, String name)
    {
		super(model, name);
    }
	
	public ModelRendererBendsChild(ModelBase model, int texOffsetX, int texOffsetY)
    {
        super(model, texOffsetX, texOffsetY);
    }
    
	public ModelRendererBendsChild setParent(ModelRendererBends parent)
	{
		this.parent = parent;
		return this;
	}
	
	@Override
    public void postRender(float p_78794_1_)
    {
		this.updateBends(p_78794_1_);
		
		this.parent.postRender(p_78794_1_);

		/*if (!this.isHidden)
        {
            if (this.showModel)
            {*/
                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F)
                    {
                        GL11.glTranslatef(this.rotationPointX * p_78794_1_, this.rotationPointY * p_78794_1_, this.rotationPointZ * p_78794_1_);
                    
                        GL11.glRotatef(-this.pre_rotation.getY(), 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(this.pre_rotation.getX(), 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(this.pre_rotation.getZ(), 0.0F, 0.0F, 1.0F);
                        GL11.glScalef(this.scaleX,this.scaleY,this.scaleZ);
                    }
                }
                else
                {
                    GL11.glTranslatef(this.rotationPointX * p_78794_1_, this.rotationPointY * p_78794_1_, this.rotationPointZ * p_78794_1_);
                    
                    GL11.glRotatef(-this.pre_rotation.getY(), 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(this.pre_rotation.getX(), 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(this.pre_rotation.getZ(), 0.0F, 0.0F, 1.0F);
                    
                    if (this.rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }
                    
                    GL11.glScalef(this.scaleX,this.scaleY,this.scaleZ);
                }
            //}
        //}
    }
}
