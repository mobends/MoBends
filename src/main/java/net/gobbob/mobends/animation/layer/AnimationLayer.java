package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.data.EntityData;

public abstract class AnimationLayer<DataType extends EntityData>
{
	/*
	 * Called by a Controller to perform a continuous
	 * animation.
	 */
	public abstract void perform(DataType entityData);
	/*
	 * Returns the actions currently being performed
	 * by the entityData. Used by BendsPacks
	 */
	public abstract String[] getActions(DataType entityData);
}
