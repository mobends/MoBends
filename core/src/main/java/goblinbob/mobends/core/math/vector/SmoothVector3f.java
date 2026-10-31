package goblinbob.mobends.core.math.vector;

import goblinbob.mobends.core.util.EnumAxis;

/**
 * A vector smoothed towards its target, each axis over its own number of ticks (see {@link #update});
 * an axis with a smoothing of 0 reaches its targets at once.
 */
public class SmoothVector3f
{

	/**
	 * Holds the interpolation start.
	 */
	public Vec3f start = new Vec3f(0, 0, 0);

	/**
	 * Holds the interpolation target.
	 */
	public Vec3f end = new Vec3f(0, 0, 0);

	/**
	 * Ticks each axis takes to reach its target (the length of a slide); 0 for at once.
	 */
	public Vec3f smoothing = new Vec3f(1, 1, 1);

	/**
	 * Holds the interpolation tween value for each axis separately.
	 */
	public Vec3f completion = new Vec3f(1, 1, 1); // <0.0F, 1.0F>; at rest until given a target

	public SmoothVector3f() {}

	public SmoothVector3f(SmoothVector3f src)
	{
		this.set(src);
	}

	public void slideTo(float x, float y, float z, float smoothing)
	{
		if (this.end.x != x || this.end.y != y || this.end.z != z)
		{
			this.start.set(this.getX(), this.getY(), this.getZ());
			this.end.set(x, y, z);
			this.completion.set(0, 0, 0);
			this.smoothing.set(smoothing, smoothing, smoothing);
			this.update(0F);
		}
	}
	
	/**
	 * Restarts the interpolation from the current value towards the target, even if the target
	 * did not change. Calling this every frame yields an exponential approach.
	 */
	public void retarget(float x, float y, float z)
	{
		this.start.set(this.getX(), this.getY(), this.getZ());
		this.end.set(x, y, z);
		this.completion.set(0, 0, 0);
		this.update(0F);
	}

	/** Restarts the interpolation of one axis from its current value towards {@code value}, over {@code smoothing} ticks. */
	public void retarget(EnumAxis axis, float value, float smoothing)
	{
		if (axis == EnumAxis.X) { this.start.x = this.getX(); this.end.x = value; this.completion.x = 0.0F; this.smoothing.x = smoothing; }
		else if (axis == EnumAxis.Y) { this.start.y = this.getY(); this.end.y = value; this.completion.y = 0.0F; this.smoothing.y = smoothing; }
		else { this.start.z = this.getZ(); this.end.z = value; this.completion.z = 0.0F; this.smoothing.z = smoothing; }
		this.update(0F);
	}

	/** Jumps one axis to {@code value}. */
	public void set(EnumAxis axis, float value)
	{
		if (axis == EnumAxis.X) this.setX(value);
		else if (axis == EnumAxis.Y) this.setY(value);
		else this.setZ(value);
	}

	/** @return whether some axis is still on its way to its target. */
	public boolean isMoving()
	{
		return this.completion.x < 1.0F || this.completion.y < 1.0F || this.completion.z < 1.0F;
	}

	/** Restarts the interpolation of one axis from its current value towards its current target. */
	public void restart(EnumAxis axis)
	{
		if (axis == EnumAxis.X) { this.start.x = this.getX(); this.completion.x = 0.0F; }
		else if (axis == EnumAxis.Y) { this.start.y = this.getY(); this.completion.y = 0.0F; }
		else { this.start.z = this.getZ(); this.completion.z = 0.0F; }
		this.update(0F);
	}

	public float getSmoothing(EnumAxis axis)
	{
		return axis == EnumAxis.X ? this.smoothing.x : axis == EnumAxis.Y ? this.smoothing.y : this.smoothing.z;
	}

	public void slideTo(EnumAxis axis, float orientation, float smoothing)
	{
		if ((axis == EnumAxis.X ? this.end.x : axis == EnumAxis.Y ? this.end.y : this.end.z) != orientation)
		{
			if (axis == EnumAxis.X)
			{
				this.start.x = this.getX();
				this.end.x = orientation;
				this.completion.x = 0.0F;
			}
			
			if (axis == EnumAxis.Y)
			{
				this.start.y = this.getY();
				this.end.y = orientation;
				this.completion.y = 0.0F;
			}
			
			if (axis == EnumAxis.Z)
			{
				this.start.z = this.getZ();
				this.end.z = orientation;
				this.completion.z = 0.0F;
			}
		}
		
		if (axis == EnumAxis.X)
			this.smoothing.x = smoothing;
		if (axis == EnumAxis.Y)
			this.smoothing.y = smoothing;
		if (axis == EnumAxis.Z)
			this.smoothing.z = smoothing;
		this.update(0F);
	}

	public void setX(float x)
	{
		this.start.x = x;
		this.end.x = x;
		this.completion.x = 1.0F;
	}

	public void setY(float y)
	{
		this.start.y = y;
		this.end.y = y;
		this.completion.y = 1.0F;
	}

	public void setZ(float z)
	{
		this.start.z = z;
		this.end.z = z;
		this.completion.z = 1.0F;
	}

	public void set(float x, float y, float z)
	{
		this.start.set(x, y, z);
		this.end.set(start);
		this.completion.set(1.0F, 1.0F, 1.0F);
	}

	public void set(SmoothVector3f other)
	{
		this.completion.set(other.completion);
		this.smoothing.set(other.smoothing);
		this.end.set(other.end);
		this.start.set(other.start);
	}

	public float getX()
	{
		return this.start.x + (this.end.x - this.start.x) * this.completion.x;
	}

	public float getY()
	{
		return this.start.y + (this.end.y - this.start.y) * this.completion.y;
	}

	public float getZ()
	{
		return this.start.z + (this.end.z - this.start.z) * this.completion.z;
	}
	
	public Vec3f getSmooth()
	{
		return new Vec3f(this.getX(), this.getY(), this.getZ());
	}

	public void update(float ticksPerFrame)
	{
		this.completion.x = advance(this.completion.x, ticksPerFrame, this.smoothing.x);
		this.completion.y = advance(this.completion.y, ticksPerFrame, this.smoothing.y);
		this.completion.z = advance(this.completion.z, ticksPerFrame, this.smoothing.z);
	}

	/** A tween's completion {@code ticks} later, when it takes {@code smoothing} ticks (0: done at once). */
	public static float advance(float completion, float ticks, float smoothing)
	{
		if (smoothing <= 0.0F)
		{
			return 1.0F;
		}
		return Math.min(completion + ticks / smoothing, 1.0F);
	}

	/** Finishes the interpolation by snapping the smooth vector straight to the end. */
	public void finish()
	{
		this.set(this.end.x, this.end.y, this.end.z);
	}

	/** A component of the interpolation target. */
	public float getEnd(EnumAxis axis)
	{
		return axis == EnumAxis.X ? this.end.x : axis == EnumAxis.Y ? this.end.y : this.end.z;
	}
}
