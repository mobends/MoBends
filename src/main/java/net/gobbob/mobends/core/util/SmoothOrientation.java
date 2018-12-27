package net.gobbob.mobends.core.util;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class SmoothOrientation
{
	public static final float PI = (float) Math.PI;
	
	protected Quaternion start;
	protected Quaternion end;
	protected Quaternion smooth;
	protected float progress;
	protected float smoothness;
	
	public SmoothOrientation()
	{
		this.start = new Quaternion();
		this.end = new Quaternion();
		this.smooth = new Quaternion();
		this.progress = 1.0F;
		this.smoothness = 1.0F;
	}

	public Quaternion getEnd()
	{
		return this.end;
	}
	
	public Quaternion getSmooth()
	{
		return this.smooth;
	}

	public void set(SmoothOrientation other)
	{
		this.start.set(other.start);
		this.end.set(other.end);
		this.smooth.set(other.smooth);
		this.progress = other.progress;
	}
	
	public SmoothOrientation setSmoothness(float smoothness)
	{
		this.smoothness = smoothness;
		return this;
	}
	
	public SmoothOrientation set(float x, float y, float z, float w)
	{
		this.start.set(x, y, z, w);
		this.start.normalise();
		this.end.set(this.start);
		this.smooth.set(this.start);
		this.progress = 0F;
		return this;
	}
	
	public SmoothOrientation orient(float angle, float x, float y, float z)
	{
		this.start.set(this.smooth);
		this.end.setFromAxisAngle(x, y, z, angle / 180F * PI);
		this.progress = 0F;
		this.updateSmooth();
		return this;
	}
	
	public SmoothOrientation orientX(float angle)
	{
		return this.orient(angle, 1F, 0F, 0F);
	}
	
	public SmoothOrientation orientY(float angle)
	{
		return this.orient(angle, 0F, 1F, 0F);
	}
	
	public SmoothOrientation orientZ(float angle)
	{
		return this.orient(angle, 0F, 0F, 1F);
	}
	
	public SmoothOrientation orientInstant(float a, float x, float y, float z)
	{
		this.end.setFromAxisAngle(x, y, z, a / 180.0F * PI);
		this.start.set(this.end);
		this.smooth.set(this.end);
		return this;
	}
	
	public SmoothOrientation orientInstantX(float angle)
	{
		return this.orientInstant(angle, 1F, 0F, 0F);
	}
	
	public SmoothOrientation orientInstantY(float angle)
	{
		return this.orientInstant(angle, 0F, 1F, 0F);
	}
	
	public SmoothOrientation orientInstantZ(float angle)
	{
		return this.orientInstant(angle, 0F, 0F, 1F);
	}
	
	public SmoothOrientation rotate(float angle, float x, float y, float z)
	{
		this.end.rotate(x, y, z, angle / 180.0F * PI);
		this.updateSmooth();
		return this;
	}
	
	public SmoothOrientation rotateX(float angle)
	{
		return this.rotate(angle, 1F, 0F, 0F);
	}
	
	public SmoothOrientation rotateY(float angle)
	{
		return this.rotate(angle, 0F, 1F, 0F);
	}
	
	public SmoothOrientation rotateZ(float angle)
	{
		return this.rotate(angle, 0F, 0F, 1F);
	}
	
	public SmoothOrientation rotateInstant(float angle, float x, float y, float z)
	{
		Quaternion rotation = new Quaternion();
		rotation.setFromAxisAngle(x, y, z, angle / 180.0F * PI);
		Quaternion.mul(rotation, this.end, this.end);
		this.start.set(this.end);
		this.smooth.set(this.end);
		return this;
	}
	
	public SmoothOrientation rotateInstantX(float angle)
	{
		return this.rotateInstant(angle, 1F, 0F, 0F);
	}
	
	public SmoothOrientation rotateInstantY(float angle)
	{
		return this.rotateInstant(angle, 0F, 1F, 0F);
	}
	
	public SmoothOrientation rotateInstantZ(float angle)
	{
		return this.rotateInstant(angle, 0F, 0F, 1F);
	}
	
	/*
	 * Rotates the orientation based on the local
	 * space, not the global space.
	 */
	public SmoothOrientation localRotate(float angle, float x, float y, float z)
	{
		Quaternion rotation = new Quaternion();
		rotation.setFromAxisAngle(x, y, z, angle / 180.0F * PI);
		Quaternion.mul(this.end, rotation, this.end);
		this.updateSmooth();
		return this;
	}
	
	public SmoothOrientation localRotateX(float angle)
	{
		return this.localRotate(angle, 1F, 0F, 0F);
	}
	
	public SmoothOrientation localRotateY(float angle)
	{
		return this.localRotate(angle, 0F, 1F, 0F);
	}
	
	public SmoothOrientation localRotateZ(float angle)
	{
		return this.localRotate(angle, 0F, 0F, 1F);
	}
	
	public SmoothOrientation orientZero()
	{
		this.start.set(this.smooth);
		this.end.setIdentity();
		this.progress = 0F;
		this.updateSmooth();
		return this;
	}
	
	public SmoothOrientation identity()
	{
		this.start.setIdentity();
		this.end.setIdentity();
		this.smooth.setIdentity();
		this.progress = 1.0F;
		return this;
	}

	public SmoothOrientation finish()
	{
		this.smooth.set(this.end);
		this.start.set(this.end);
		this.progress = 1.0F;
		this.updateSmooth();
		return this;
	}
	
	public SmoothOrientation orient(EnumAxis axis, float angle)
	{
		if (axis == EnumAxis.X)
			this.orientX(angle);
		else if (axis == EnumAxis.Y)
			this.orientY(angle);
		else if (axis == EnumAxis.Z)
			this.orientZ(angle);
		return this;
	}
	
	public SmoothOrientation rotate(EnumAxis axis, float angle)
	{
		if (axis == EnumAxis.X)
			this.rotateX(angle);
		else if (axis == EnumAxis.Y)
			this.rotateY(angle);
		else if (axis == EnumAxis.Z)
			this.rotateZ(angle);
		return this;
	}
	
	public SmoothOrientation localRotate(EnumAxis axis, float angle)
	{
		if (axis == EnumAxis.X)
			this.localRotateX(angle);
		else if (axis == EnumAxis.Y)
			this.localRotateY(angle);
		else if (axis == EnumAxis.Z)
			this.localRotateZ(angle);
		return this;
	}
	
	public void update(float ticksPerFrame)
	{
		this.progress += ticksPerFrame * this.smoothness;
		this.progress = Math.min(this.progress, 1.0F);
		this.updateSmooth();
	}
	
	public void updateSmooth()
	{
		this.smooth.set(this.start.x + (this.end.x-this.start.x) * this.progress,
				this.start.y + (this.end.y-this.start.y) * this.progress,
				this.start.z + (this.end.z-this.start.z) * this.progress,
				this.start.w + (this.end.w-this.start.w) * this.progress);
		this.smooth.normalise();
	}
}
