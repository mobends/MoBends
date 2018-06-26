package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class HardAnimationLayer extends AnimationLayer
{
	protected AnimationBit performedBit;
	protected AnimationBit previousBit;
	
	public void playBit(AnimationBit bit, EntityData entityData)
	{
		this.previousBit = this.performedBit;
		this.performedBit = bit;
		this.performedBit.setupForPlay(this, entityData);
	}
	
	public void playOrContinueBit(AnimationBit bit, EntityData entityData)
	{
		if (!this.isPlaying(bit))
			this.playBit(bit, entityData);
	}

	@Override
	public void perform(EntityData entityData)
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
	public String[] getActions(EntityData entityData)
	{
		if (this.isPlaying())
		{
			return this.getPerformedBit().getActions(entityData);
		}
		return new String[] {};
	}
}
