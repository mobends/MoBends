package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.animation.bit.biped.JumpAnimationBit;
import net.gobbob.mobends.data.EntityData;

public abstract class AnimationLayer
{
	/*
	 * Called by a Controller to perform a continuous
	 * animation.
	 */
	public abstract void perform(EntityData entityData);
	/*
	 * Returns the actions currently being performed
	 * by the entityData. Used by BendsPacks
	 */
	public abstract String[] getActions(EntityData entityData);
}
