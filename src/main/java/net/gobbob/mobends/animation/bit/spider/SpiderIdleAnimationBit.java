package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class SpiderIdleAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "idle" };
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		
	}
}
