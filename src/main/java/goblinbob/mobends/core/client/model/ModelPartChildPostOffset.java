package goblinbob.mobends.core.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Vector3f;

/*
 * This part is used, when accessories are rendered using it's postRender() method.
 * It simply offsets those accessories by a certain transformation.
 */
public class ModelPartChildPostOffset extends ModelPartChild
{
	
	/*
	 * The amount to offset whatever is using the postRender method.
	 */
	protected Vector3f postOffset = new Vector3f(0.0F, 0.0F, 0.0F);

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
	public void applyPostTransform(float scale)
	{
		GlStateManager.translate(this.postOffset.x * scale, this.postOffset.y * scale, this.postOffset.z * scale);
	}
	
}
