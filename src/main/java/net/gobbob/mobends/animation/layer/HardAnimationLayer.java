package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class HardAnimationLayer<DataType extends EntityData> extends AnimationLayer<DataType>
{
	protected AnimationBit<DataType> performedBit;
	protected AnimationBit<DataType> previousBit;
	
	public void playBit(AnimationBit bit, DataType entityData)
	{
		this.previousBit = this.performedBit;
		this.performedBit = bit;
		this.performedBit.setupForPlay(this, entityData);
	}
	
	public void playOrContinueBit(AnimationBit bit, DataType entityData)
	{
		if (!this.isPlaying(bit))
			this.playBit(bit, entityData);
	}

	@Override
	public void perform(DataType entityData)
	{
		if (performedBit != null)
			performedBit.perform(entityData);
	}

	public boolean isPlaying(AnimationBit bit)
	{
		return bit == this.performedBit;
	}
	
	public boolean isPlaying()
	{
		return this.performedBit != null;
	}

	public void clearAnimation()
	{
		this.performedBit = null;
	}

	public AnimationBit getPerformedBit()
	{
		return this.performedBit;
	}

	@Override
	public String[] getActions(DataType entityData)
	{
		if (this.isPlaying())
		{
			return this.getPerformedBit().getActions(entityData);
		}
		return new String[] {};
	}
}
