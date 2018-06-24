package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class HardAnimationLayer extends AnimationLayer
{
	protected AnimationBit performedBit;

	public void playBit(AnimationBit bit)
	{
		this.performedBit = bit;
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (performedBit != null)
			performedBit.perform(entityData);
	}

	public boolean isPlaying(AnimationBit bit)
	{
		return bit == performedBit;
	}
}
