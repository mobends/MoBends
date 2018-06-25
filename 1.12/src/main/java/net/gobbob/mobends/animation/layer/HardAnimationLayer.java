package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class HardAnimationLayer extends AnimationLayer
{
	protected AnimationBit performedBit;

	public void playBit(AnimationBit bit)
	{
		this.performedBit = bit;
		this.performedBit.setupForPlay(this);
	}
	
	public void playOrContinueBit(AnimationBit bit)
	{
		if (!this.isPlaying(bit))
			this.playBit(bit);
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

	public void clearAnimation()
	{
		this.performedBit = null;
	}
}
