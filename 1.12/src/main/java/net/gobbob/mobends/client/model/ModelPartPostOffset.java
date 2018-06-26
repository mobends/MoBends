package net.gobbob.mobends.client.model;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * This part is used, when accessories are rendered using it's postRender() method.
 * It simply offsets those accessories by a certain transformation.
 */
public class ModelPartPostOffset extends ModelPart
{
	/*
	 * The amount to offset whatever is using the postRender method.
	 */
	public Vector3f postOffset = new Vector3f(0.0F, 0.0F, 0.0F);

	public ModelPartPostOffset(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
	{
		super(model, register, texOffsetY, texOffsetY);
	}

	public ModelPartPostOffset(ModelBase model, boolean register)
	{
		super(model, register);
	}

	public ModelPartPostOffset(ModelBase model, int texOffsetX, int texOffsetY)
	{
		super(model, texOffsetX, texOffsetY);
	}

	public ModelPartPostOffset setPostOffset(float x, float y, float z)
	{
		this.postOffset.set(x, y, z);
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postRender(float scale)
	{
		super.postRender(scale);

		GlStateManager.translate(this.postOffset.x * scale, this.postOffset.y * scale, this.postOffset.z * scale);
	}
}
