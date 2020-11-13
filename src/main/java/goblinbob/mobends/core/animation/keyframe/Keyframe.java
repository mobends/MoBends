package goblinbob.mobends.core.animation.keyframe;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.Arrays;

public class Keyframe implements ISerializable
{
	private static final byte HAS_POSITION = 1;
	private static final byte HAS_ROTATION = 2;
	private static final byte HAS_SCALE = 4;
	private static final float SKIP_THRESHOLD = 0.0000001F;

	public float[] position;
	// X, Y, Z, W
	public float[] rotation;

	public float[] scale;
	
	public void mirrorRotationYZ()
	{
		rotation[1] *= -1;
		rotation[2] *= -1;
	}

	public void swapRotationYZ()
	{
		float y = rotation[1];
		rotation[1] = rotation[2];
		rotation[2] = y;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Keyframe))
			return false;

		Keyframe other = (Keyframe) obj;

		return Arrays.equals(other.position, this.position) &&
				Arrays.equals(other.rotation, this.rotation) &&
				Arrays.equals(other.scale, this.scale);
	}

	@Override
	public void serialize(ISerialOutput out)
	{
		byte flags = 0;

		if (this.position != null && this.position.length == 3 && (
			Math.abs(this.position[0]) >= SKIP_THRESHOLD ||
			Math.abs(this.position[1]) >= SKIP_THRESHOLD ||
			Math.abs(this.position[2]) >= SKIP_THRESHOLD
		)) {
			flags |= HAS_POSITION;
		}

		if (this.rotation != null && this.rotation.length == 4 && (
			Math.abs(this.rotation[0]) >= SKIP_THRESHOLD ||
			Math.abs(this.rotation[1]) >= SKIP_THRESHOLD ||
			Math.abs(this.rotation[2]) >= SKIP_THRESHOLD ||
			Math.abs(this.rotation[3] - 1) >= SKIP_THRESHOLD
		)) {
			flags |= HAS_ROTATION;
		}

		if (this.scale != null && this.scale.length == 3 && (
			Math.abs(this.scale[0]) >= SKIP_THRESHOLD ||
			Math.abs(this.scale[1]) >= SKIP_THRESHOLD ||
			Math.abs(this.scale[2]) >= SKIP_THRESHOLD
		)) {
			flags |= HAS_SCALE;
		}

		out.writeByte(flags);

		if ((flags & HAS_POSITION) != 0)
		{
			out.writeFloat(this.position[0]);
			out.writeFloat(this.position[1]);
			out.writeFloat(this.position[2]);
		}

		if ((flags & HAS_ROTATION) != 0)
		{
			out.writeFloat(this.rotation[0]);
			out.writeFloat(this.rotation[1]);
			out.writeFloat(this.rotation[2]);
			out.writeFloat(this.rotation[3]);
		}

		if ((flags & HAS_SCALE) != 0)
		{
			out.writeFloat(this.scale[0]);
			out.writeFloat(this.scale[1]);
			out.writeFloat(this.scale[2]);
		}
	}

	public static Keyframe deserialize(ISerialInput in) throws IOException
	{
		Keyframe frame = new Keyframe();

		byte flags = in.readByte();
		if ((flags & HAS_POSITION) != 0)
		{
			frame.position = SerialHelper.deserializeFloats(3, in);
		}
		else
		{
			frame.position = new float[] { 0, 0, 0 };
		}

		if ((flags & HAS_ROTATION) != 0)
		{
			frame.rotation = SerialHelper.deserializeFloats(4, in);
		}
		else
		{
			frame.rotation = new float[] { 0, 0, 0, 1 };
		}

		if ((flags & HAS_SCALE) != 0)
		{
			frame.scale = SerialHelper.deserializeFloats(3, in);
		}
		else
		{
			frame.scale = new float[] { 1, 1, 1 };
		}

		return frame;
	}
}
