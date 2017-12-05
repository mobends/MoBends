package net.gobbob.mobends.animation.bit;

import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;

public abstract class AnimationBit
{
	/*
	 * The layer that this bit is performed by.
	 * Used to callback, e.g. when the animation is finished.
	 * */
	protected AnimationLayer layer;
	
	public AnimationBit(AnimationLayer layer) {
		this.layer = layer;
	}
	
	public abstract void perform(EntityData entityData);
}