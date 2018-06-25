package net.gobbob.mobends.animation.bit;

import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;

public abstract class AnimationBit
{
	/*
	 * The layer that this bit is performed by.
	 * Used to callback, e.g. when the animation is finished.
	 * */
	protected AnimationLayer layer;
	
	/*
	 * Called by the AnimationLayer before it plays
	 * this bit.
	 */
	public void setupForPlay(AnimationLayer layer)
	{
		this.layer = layer;
	}
	
	public abstract void perform(EntityData entityData);
}