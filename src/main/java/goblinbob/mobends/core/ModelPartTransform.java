package goblinbob.mobends.core;

import goblinbob.bendslib.math.IQuaternion;
import goblinbob.bendslib.math.Quaternion;
import goblinbob.bendslib.math.vector.IVec3f;
import goblinbob.bendslib.math.vector.Vec3f;

/**
 * Used for manipulating the transform of things that are
 * going to postRender this part.
 */
public class ModelPartTransform implements IModelPart
{
	public Vec3f position;
	public Vec3f scale;
	public Vec3f offset;
	public Quaternion rotation;
	/**
	 * The scale at which animation position offset is applied, used for child models.
	 */
	public float offsetScale = 1.0F;
	/**
	 * Offset applied before the parent transformation.
	 */
	public Vec3f globalOffset = new Vec3f();

	public ModelPartTransform()
	{
		this.position = new Vec3f();
		this.scale = new Vec3f(1, 1, 1);
		this.offset = new Vec3f();
		this.rotation = new Quaternion(Quaternion.IDENTITY);
	}

	@Override
	public IVec3f getPosition() { return this.position; }

	@Override
	public IVec3f getScale() { return this.scale; }

	@Override
	public IVec3f getOffset() { return this.offset; }

	@Override
	public IQuaternion getRotation() { return this.rotation; }

	@Override
	public float getOffsetScale() { return this.offsetScale; }
	@Override
	public IVec3f getGlobalOffset()
	{
		return globalOffset;
	}

	@Override
	public void syncUp(IModelPart part)
	{
		if(part == null)
			return;

		this.position.set(part.getPosition());
		this.rotation.set(part.getRotation());
		this.offset.set(part.getOffset());
		this.scale.set(part.getScale());
		this.offsetScale = part.getOffsetScale();
	}

	@Override
	public boolean isShowing()
	{
		return true;
	}

	@Override
	public void setVisible(boolean showModel)
	{
		// Do nothing
	}
}
