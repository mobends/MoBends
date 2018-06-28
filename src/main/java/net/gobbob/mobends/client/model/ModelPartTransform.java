package net.gobbob.mobends.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

/*
 * Used for manipulating the transform of things that are
 * going to postRender this part.
 */
public class ModelPartTransform implements IModelPart
{
	public Vector3f position;
	public Vector3f scale;
	public SmoothVector3f rotation;
	public SmoothVector3f preRotation;
	
	public ModelPartTransform()
	{
		this.position = new Vector3f();
		this.scale = new Vector3f(1, 1, 1);
		this.rotation = new SmoothVector3f();
		this.preRotation = new SmoothVector3f();
	}
	
	@Override
	public void applyStandaloneTransform(float scale)
	{
		this.applyOwnTransform(scale);
	}

	@Override
	public void renderPart(float scale)
	{
		//Since this is just a transform, do nothing.
	}
	
	@Override
	public void renderJustPart(float scale)
	{
	}

	@Override
	public void update(float ticksPerFrame)
	{
		this.rotation.update(ticksPerFrame);
		this.preRotation.update(ticksPerFrame);
	}

	@Override
	public Vector3f getPosition() { return this.position; }
	@Override
	public Vector3f getScale() { return this.scale; }
	@Override
	public SmoothVector3f getRotation() { return this.rotation; }
	@Override
	public SmoothVector3f getPreRotation() { return this.preRotation; }

	@Override
	public void syncUp(IModelPart part)
	{
		if(part == null)
			return;
		this.position.set(part.getPosition());
		this.rotation.set(part.getRotation());
		this.preRotation.set(part.getPreRotation());
		this.scale.set(part.getScale());
	}

	@Override
	public boolean isShowing()
	{
		return true;
	}

	@Override
	public void applyOwnTransform(float scale)
	{
		if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
        	GlStateManager.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
        
		if (this.preRotation.getZ() != 0.0F)
            GlStateManager.rotate(this.preRotation.getZ(), 0F, 0F, 1F);
        if (this.preRotation.getY() != 0.0F)
            GlStateManager.rotate(this.preRotation.getY(), 0F, 1F, 0F);
        if (this.preRotation.getX() != 0.0F)
            GlStateManager.rotate(this.preRotation.getX(), 1F, 0F, 0F);
		
        if (this.rotation.getZ() != 0.0F)
            GlStateManager.rotate(this.rotation.getZ(), 0F, 0F, 1F);
        if (this.rotation.getY() != 0.0F)
            GlStateManager.rotate(this.rotation.getY(), 0F, 1F, 0F);
        if (this.rotation.getX() != 0.0F)
            GlStateManager.rotate(this.rotation.getX(), 1F, 0F, 0F);
        
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

	@Override
	public void setVisible(boolean showModel)
	{
		// Do nothing
	}
}
