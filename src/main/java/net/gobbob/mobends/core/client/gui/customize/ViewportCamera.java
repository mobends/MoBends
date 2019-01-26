package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.IVec3fRead;
import net.gobbob.mobends.core.util.Vec3f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class ViewportCamera
{
	
	/**
	 * The camera's origin relative to the viewport's origin.
	 */
	private Vec3f position;
	
	/**
	 * These are calculated based on the current orientation of the camera.
	 * Should be updated any time the orientation changes.
	 */
	private Vec3f forward;
	private Vec3f up;
	private Vec3f right;
	
	private float angleYaw, 	// around the Y axis
				  anglePitch; 	// around the X axis
	
	private Vec3f anchorPoint;
	private float anchorDistance = 0;
	private boolean anchored = false;
	
	public ViewportCamera(float posX, float posY, float posZ, float angleYaw, float anglePitch)
	{
		this.position = new Vec3f(posX, posY, posZ);
		this.anchorPoint = new Vec3f();
		this.angleYaw = angleYaw;
		this.anglePitch = anglePitch;
		
		this.forward = new Vec3f();
		this.up = new Vec3f();
		this.right = new Vec3f();
	}
	
	public void setPosition(float x, float y, float z)
	{
		this.position.set(x, y, z);
	}
	
	/**
	 * Moves the camera in world space.
	 * @param x The amount to move by in the x axis.
	 * @param y The amount to move by in the y axis.
	 * @param z The amount to move by in the z axis.
	 */
	public void moveGlobal(float x, float y, float z)
	{
		this.position.add(x, y, z);
	}
	
	public void moveForward(float amount)
	{
		if (this.anchored)
		{
			this.anchorPoint.add(this.forward.x * amount, this.forward.y * amount, this.forward.z * amount);
			this.updateAsAnchored();
		}
		// TODO Implement
	}
	
	public void moveSideways(float amount)
	{
		// TODO Implement
	}
	
	public void moveUp(float amount)
	{
		// TODO Implement
	}
	
	public void zoomInOrOut(float amount)
	{
		this.anchorDistance += (amount) * ( 1 + this.anchorDistance * 4F) * 0.04F;
		this.anchorDistance = Math.max(0.1F, this.anchorDistance);
		this.updateAsAnchored();
	}
	
	public void setAnchorDistance(float distance)
	{
		this.anchorDistance = distance;
	}
	
	public void rotateYaw(float angle)
	{
		this.angleYaw += angle;
		
		if (this.anchored)
		{
			this.updateAsAnchored();
		}
		
		this.updateLocalSpace();
	}
	
	public void rotatePitch(float angle)
	{
		this.anglePitch += angle;
		if (this.anglePitch < -90.0F)
			this.anglePitch = -90.0F;
		if (this.anglePitch > 90.0F)
			this.anglePitch = 90.0F;
		
		if (this.anchored)
		{
			this.updateAsAnchored();
		}
		
		this.updateLocalSpace();
	}
	
	public void applyTransform()
	{
		GlStateManager.rotate(this.anglePitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(this.angleYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-this.position.x, -this.position.y, -this.position.z);
	}

	public void anchorTo(float x, float y, float z, float distance)
	{
		this.anchorPoint.set(x, y, z);
		this.anchorDistance = distance;
		this.anchored = true;
		this.updateAsAnchored();
	}
	
	public void unanchor()
	{
		this.anchored = false;
	}
	
	public void updateAsAnchored()
	{
		final float yaw = this.angleYaw / 180F * GUtil.PI;
		final float pitch = this.anglePitch / 180F * GUtil.PI;
		
		final float cosPitch = MathHelper.cos(pitch);
		final float dX = MathHelper.sin(yaw) * cosPitch;
		final float dZ = -MathHelper.cos(yaw) * cosPitch;
		final float dY = -MathHelper.sin(pitch);
		
		this.position.set(
			this.anchorPoint.x - dX * this.anchorDistance,
			this.anchorPoint.y - dY * this.anchorDistance,
			this.anchorPoint.z - dZ * this.anchorDistance
		);
	}
	
	private void updateLocalSpace()
	{
		final float yaw = this.angleYaw / 180F * GUtil.PI;
		final float pitch = this.anglePitch / 180F * GUtil.PI;
		final float sinPitch = MathHelper.sin(pitch);
		final float cosPitch = MathHelper.cos(pitch);
		
		this.forward.set(
			MathHelper.sin(yaw) * cosPitch,
			-MathHelper.sin(pitch),
			-MathHelper.cos(yaw) * cosPitch
		);
		
		this.up.set(
			MathHelper.sin(yaw) * sinPitch,
			MathHelper.cos(pitch),
			-MathHelper.cos(yaw) * sinPitch
		);
		
		this.right.set(
			MathHelper.cos(yaw),
			0,
			MathHelper.sin(yaw)
		);
	}
	
	public void lookAt(float x, float y, float z)
	{
		float dX = this.anchorPoint.x - this.position.x;
		float dY = this.anchorPoint.y - this.position.y;
		float dZ = this.anchorPoint.z - this.position.z;
		
		this.lookInDirection(dX, dY, dZ);
	}
	
	public void lookInDirection(float x, float y, float z)
	{
		this.angleYaw = (float) MathHelper.atan2(x, z) / GUtil.PI * 180F;
		float xzLen = MathHelper.sqrt(x * x + z * z);
		this.anglePitch = (float) MathHelper.atan2(xzLen, y) / GUtil.PI * 180F;
		
		this.updateLocalSpace();
	}
	
	public IVec3fRead getForward()
	{
		return this.forward;
	}
	
	public IVec3fRead getUp()
	{
		return this.up;
	}
	
	public IVec3fRead getRight()
	{
		return this.right;
	}
	
}
