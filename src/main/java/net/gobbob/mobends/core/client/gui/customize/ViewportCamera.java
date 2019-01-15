package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class ViewportCamera
{
	private float posX, posY, posZ;
	private float angleYaw, 	// around the Y axis
				  anglePitch; 	// around the X axis
	
	private float anchorPointX,
				  anchorPointY,
				  anchorPointZ;
	private float anchorDistance = 0;
	private boolean anchored = false;
	
	public ViewportCamera(float posX, float posY, float posZ, float angleYaw, float anglePitch)
	{
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.angleYaw = angleYaw;
		this.anglePitch = anglePitch;
	}
	
	public void setPosition(float x, float y, float z)
	{
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}
	
	/**
	 * Moves the camera in world space.
	 * @param x The amount to move by in the x axis.
	 * @param y The amount to move by in the y axis.
	 * @param z The amount to move by in the z axis.
	 */
	public void moveGlobal(float x, float y, float z)
	{
		this.posX += x;
		this.posY += y;
		this.posZ += z;
	}
	
	public void moveForward(float amount)
	{
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
	}
	
	public void applyTransform()
	{
		GlStateManager.rotate(this.anglePitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(this.angleYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(-this.posX, -this.posY, -this.posZ);
	}

	public void anchorTo(float x, float y, float z, float distance)
	{
		this.anchorPointX = x;
		this.anchorPointY = y;
		this.anchorPointZ = z;
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
		float yaw = this.angleYaw / 180F * GUtil.PI;
		float pitch = this.anglePitch / 180F * GUtil.PI;
		
		float cosPitch = MathHelper.cos(pitch);
		float dX = MathHelper.sin(yaw) * cosPitch;
		float dZ = -MathHelper.cos(yaw) * cosPitch;
		float dY = -MathHelper.sin(pitch);
		
		this.posX = this.anchorPointX - dX * this.anchorDistance;
		this.posZ = this.anchorPointZ - dZ * this.anchorDistance;
		this.posY = this.anchorPointY - dY * this.anchorDistance;
	}
	
	public void lookAt(float x, float y, float z)
	{
		float dX = this.anchorPointX - this.posX;
		float dY = this.anchorPointY - this.posY;
		float dZ = this.anchorPointZ - this.posZ;
		
		this.lookInDirection(dX, dY, dZ);
	}
	
	public void lookInDirection(float x, float y, float z)
	{
		this.angleYaw = (float) MathHelper.atan2(x, z) / GUtil.PI * 180F;
		float xzLen = MathHelper.sqrt(x * x + z * z);
		this.anglePitch = (float) MathHelper.atan2(xzLen, y) / GUtil.PI * 180F;
	}
}
