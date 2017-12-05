package net.gobbob.mobends.animation.layer;

import net.gobbob.mobends.data.EntityData;

public abstract class AnimationLayer
{
	public AnimationLayer() {
		
	}
	
	public abstract void perform(EntityData entityData);
}
