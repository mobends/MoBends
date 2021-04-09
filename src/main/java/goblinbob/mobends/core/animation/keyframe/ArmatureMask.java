package goblinbob.mobends.core.animation.keyframe;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes which bones of an armature should be affected, and which should not.
 * 
 * @author GoblinBob
 *
 */
public class ArmatureMask implements ISerializable
{
	private Mode mode;
	private Set<String> includedParts;
	private Set<String> excludedParts;

	public ArmatureMask(Mode mode)
	{
		this.mode = mode;
		this.includedParts = new HashSet<>();
		this.excludedParts = new HashSet<>();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ArmatureMask))
			return false;

		ArmatureMask other = (ArmatureMask) obj;
		return other.mode.equals(this.mode) &&
				other.includedParts.equals(this.includedParts) &&
				other.excludedParts.equals(this.excludedParts);
	}

	public boolean doesAllow(String bone)
	{
		switch (this.mode)
		{
			case INCLUDE_ONLY:
				return this.includedParts.contains(bone);
			case EXCLUDE_ONLY:
				return !this.excludedParts.contains(bone);
			default:
				return true;
		}
	}

	@Override
	public void serialize(ISerialOutput out)
	{
		out.writeByte((byte) this.mode.ordinal());
	}

	public static <D extends IEntityData, C extends ISerialContext<C, D>> ArmatureMask deserialize(C context, ISerialInput in) throws IOException
	{
		Mode mode = Mode.values()[in.readByte()];

		return new ArmatureMask(mode);
	}

	public enum Mode
	{
		INCLUDE_ONLY, EXCLUDE_ONLY
	}
}
