package net.gobbob.mobends.core.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * This part is used, when accessories are rendered using it's postRender() method.
 * It simply offsets those accessories by a certain transformation.
 */
public class ModelPartChildPostOffset extends ModelPartChild
{
	/*
	 * The amount to offset whatever is using the postRender method.
	 */
	public Vector3f postOffset = new Vector3f(0.0F, 0.0F, 0.0F);

	public ModelPartChildPostOffset(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, register, texOffsetY, texOffsetY);
	}

	public ModelPartChildPostOffset(ModelBase model, boolean register)
	{
		super(model, register);
	}

	public ModelPartChildPostOffset(ModelBase model, int texOffsetX, int texOffsetY)
	{
		super(model, texOffsetX, texOffsetY);
	}

	public ModelPartChildPostOffset setPostOffset(float x, float y, float z)
	{
		this.postOffset.set(x, y, z);
		return this;
	}
	
	@Override
	public void propagateTransform(float scale)
	{
		super.propagateTransform(scale);
		this.applyPostTransform(scale);
	}
	
	@Override
	public void applyPostTransform(float scale)
	{
		GlStateManager.translate(this.postOffset.x * scale, this.postOffset.y * scale, this.postOffset.z * scale);
	}
}
