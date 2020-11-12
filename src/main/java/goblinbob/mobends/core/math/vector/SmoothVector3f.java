package goblinbob.mobends.core.math.vector;

import goblinbob.mobends.core.util.EnumAxis;

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
	 * Holds the interpolation speed for each axis separately.
	 */
	public Vec3f smoothness = new Vec3f(1, 1, 1);

	/**
	 * Holds the interpolation tween value for each axis separately.
	 */
	public Vec3f completion = new Vec3f(0, 0, 0); // <0.0F, 1.0F>

	public SmoothVector3f() {}

	public SmoothVector3f(SmoothVector3f src)
	{
		this.set(src);
	}

	public void slideTo(float x, float y, float z, float smoothness)
	{
		if (this.end.x != x || this.end.y != y || this.end.z != z)
		{
			this.start.set(this.getX(), this.getY(), this.getZ());
			this.end.set(x, y, z);
			this.completion.set(0, 0, 0);
			this.smoothness.set(smoothness, smoothness, smoothness);
		}
	}
	
	public void slideTo(Vec3f orientation, float smoothness)
	{
		this.slideTo(orientation.x, orientation.y, orientation.z, smoothness);
	}
	
	public void slideTo(Vec3f orientation)
	{
		this.slideTo(orientation, 1.0F);
	}

	public void slideToZero(float smoothness)
	{
		this.slideTo(0, 0, 0, smoothness);
	}
	
	public void slideToZero()
	{
		this.slideToZero(1.0F);
	}

	public void slideTo(EnumAxis axis, float orientation, float smoothness)
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
			this.smoothness.x = smoothness;
		if (axis == EnumAxis.Y)
			this.smoothness.y = smoothness;
		if (axis == EnumAxis.Z)
			this.smoothness.z = smoothness;
	}

	public void slideX(float orientation, float smoothness)
	{
		if (this.end.x != orientation)
		{
			this.start.x = this.getX();
			this.end.x = orientation;
			this.completion.x = 0.0F;
		}
		this.smoothness.x = smoothness;
	}

	public void slideY(float argY, float argSmooth)
	{
		if (this.end.y != argY)
		{
			this.start.y = this.getY();
			this.end.y = argY;
			this.completion.y = 0.0F;
		}
		this.smoothness.y = argSmooth;
	}

	public void slideZ(float argZ, float argSmooth)
	{
		if (this.end.z != argZ)
		{
			this.start.z = this.getZ();
			this.end.z = argZ;
			this.completion.z = 0.0F;
		}
		this.smoothness.z = argSmooth;
	}

	public void slideX(float x)
	{
		this.slideX(x, 0.6f);
	}

	public void slideY(float y)
	{
		this.slideY(y, 0.6f);
	}

	public void slideZ(float z)
	{
		this.slideZ(z, 0.6f);
	}
	
	public void add(float x, float y, float z)
	{
		this.start.set(this.getX(), this.getY(), this.getZ());
		this.completion.set(0, 0, 0);
		this.end.x += x;
		this.end.y += y;
		this.end.z += z;
	}
	
	public void addX(float x)
	{
		this.end.x += x;
	}
	
	public void addY(float y)
	{
		this.end.y += y;
	}
	
	public void addZ(float z)
	{
		this.end.z += z;
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
		this.smoothness.set(other.smoothness);
		this.end.set(other.end);
		this.start.set(other.start);
	}

	public void limitDistanceTo(SmoothVector3f other, float maxDistance)
	{
		final Vec3f diff = new Vec3f(end.x - other.end.x, end.y - other.end.y, end.z - other.end.z);
		final float sqDist = diff.lengthSq();

		if (sqDist > maxDistance * maxDistance)
		{
			VectorUtils.normalize(diff);
			end.set(
				other.end.x + diff.x * maxDistance,
				other.end.y + diff.y * maxDistance,
				other.end.z + diff.z * maxDistance
			);
		}
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
		this.completion.x += ticksPerFrame * this.smoothness.x;
		this.completion.y += ticksPerFrame * this.smoothness.y;
		this.completion.z += ticksPerFrame * this.smoothness.z;
		this.completion.x = Math.min(this.completion.x, 1.0F);
		this.completion.y = Math.min(this.completion.y, 1.0F);
		this.completion.z = Math.min(this.completion.z, 1.0F);
	}

	public float getNextX(float ticksPerFrame)
	{
		float c = this.completion.x + ticksPerFrame * this.smoothness.x;
		float v = this.start.x + (this.end.x - this.start.x) * this.completion.x;
		if (completion.x >= 1.0f)
			v = this.end.x;
		return v;
	}

	public float getNextY(float ticksPerFrame)
	{
		float c = this.completion.y + ticksPerFrame * this.smoothness.y;
		float v = this.start.y + (this.end.y - this.start.y) * this.completion.y;
		if (completion.y >= 1.0f)
			v = this.end.y;
		return v;
	}

	public float getNextZ(float ticksPerFrame)
	{
		float c = this.completion.z + ticksPerFrame * this.smoothness.z;
		float v = this.start.z + (this.end.z - this.start.z) * this.completion.z;
		if (completion.z >= 1.0f)
			v = this.end.z;
		return v;
	}

	/**
	 * Finished the interpolation by snapping the smooth vector straight to the end.
	 */
	public void finish()
	{
		this.set(this.end.x, this.end.y, this.end.z);
	}

	/**
	 * Get's a component from the interpolation target based on the axis.
	 * @param axis
	 * @return
	 */
	public float getEnd(EnumAxis axis)
	{
		return axis == EnumAxis.X ? this.end.x : axis == EnumAxis.Y ? this.end.y : this.end.z;
	}
}
