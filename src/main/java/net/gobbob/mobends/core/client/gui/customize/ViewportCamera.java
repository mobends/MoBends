package net.gobbob.mobends.core.client.gui.customize;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import net.gobbob.mobends.core.math.Ray;
import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.matrix.MatrixUtils;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3d;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.math.vector.Vec4f;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.GlHelper;
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
	
	private float angleYaw, 	// around the Y axis in radians
				  anglePitch; 	// around the X axis in radians
	
	private Vec3f anchorPoint;
	private float anchorDistance = 0;
	private boolean anchored = false;
	
	private Mat4x4d rotationMatrix;
	private FloatBuffer rotationMatrixBuffer;
	
	public ViewportCamera(float posX, float posY, float posZ, float angleYaw, float anglePitch)
	{
		this.position = new Vec3f(posX, posY, posZ);
		this.anchorPoint = new Vec3f();
		this.angleYaw = angleYaw;
		this.anglePitch = anglePitch;
		
		this.forward = new Vec3f();
		this.up = new Vec3f();
		this.right = new Vec3f();
		this.rotationMatrix = new Mat4x4d();
		this.rotationMatrixBuffer = BufferUtils.createFloatBuffer(16);
		this.updateLocalSpace();
		this.updateViewMatrix();
	}

	public void setPosition(float x, float y, float z)
	{
		this.position.set(x, y, z);
		this.updateViewMatrix();
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
		this.updateViewMatrix();
	}
	
	public void moveForward(float amount)
	{
		if (this.anchored)
		{
			this.anchorPoint.add(this.forward.x * amount, this.forward.y * amount, this.forward.z * amount);
			this.updateAsAnchored();
		}
	}
	
	public void moveSideways(float amount)
	{
		if (this.anchored)
		{
			this.anchorPoint.add(this.right.x * amount, this.right.y * amount, this.right.z * amount);
			this.updateAsAnchored();
		}
	}
	
	public void moveUp(float amount)
	{
		if (this.anchored)
		{
			this.anchorPoint.add(this.up.x * amount, this.up.y * amount, this.up.z * amount);
			this.updateAsAnchored();
		}
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
		GlStateManager.multMatrix(this.rotationMatrixBuffer);
		//GlStateManager.translate(-this.position.x, -this.position.y, -this.position.z);
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
		final float cosPitch = MathHelper.cos(this.anglePitch);
		final float dX = MathHelper.sin(this.angleYaw) * cosPitch;
		final float dZ = -MathHelper.cos(this.angleYaw) * cosPitch;
		final float dY = -MathHelper.sin(this.anglePitch);
		
		this.position.set(
			this.anchorPoint.x - dX * this.anchorDistance,
			this.anchorPoint.y - dY * this.anchorDistance,
			this.anchorPoint.z - dZ * this.anchorDistance
		);
		
		this.updateViewMatrix();
	}
	
	private void updateLocalSpace()
	{
		final float sinPitch = MathHelper.sin(this.anglePitch);
		final float cosPitch = MathHelper.cos(this.anglePitch);
		
		this.forward.set(
			MathHelper.sin(this.angleYaw) * cosPitch,
			-MathHelper.sin(this.anglePitch),
			-MathHelper.cos(this.angleYaw) * cosPitch
		);
		
		this.up.set(
			MathHelper.sin(this.angleYaw) * sinPitch,
			MathHelper.cos(this.anglePitch),
			-MathHelper.cos(this.angleYaw) * sinPitch
		);
		
		this.right.set(
			MathHelper.cos(this.angleYaw),
			0,
			MathHelper.sin(this.angleYaw)
		);
	}
	
	private void updateViewMatrix()
	{
		MatrixUtils.identity(this.rotationMatrix);
		TransformUtils.rotate(this.rotationMatrix, this.anglePitch, 1.0, 0.0, 0.0, this.rotationMatrix);
		TransformUtils.rotate(this.rotationMatrix, this.angleYaw, 0.0, 1.0, 0.0, this.rotationMatrix);
		TransformUtils.translate(this.rotationMatrix, -this.position.x, -this.position.y, -this.position.z, this.rotationMatrix);
		MatrixUtils.matToGlMatrix(this.rotationMatrix, this.rotationMatrixBuffer);
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
		this.angleYaw = (float) MathHelper.atan2(x, z);
		float xzLen = MathHelper.sqrt(x * x + z * z);
		this.anglePitch = (float) MathHelper.atan2(xzLen, y);
		
		this.updateLocalSpace();
	}
	
	public IVec3fRead getPosition()
	{
		return this.position;
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
	
	public Ray getRayFromMouse(int mx, int my, int width, int height)
	{
		float nx = (float)mx / (float)width * 2 - 1;
		float ny = 1 - (float)my / (float)height * 2;
		
		// Z needs to go forward. W is 1 by default
		Vec4f clip = new Vec4f(nx, ny, -1, 1);
		
		return new Ray(0, 0, 0, 0, 0, 0);
	}
	
}
