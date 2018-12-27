package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.data.SpiderData;

public class SpiderIdleAnimationBit extends AnimationBit<SpiderData>
{
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return new String[] { "idle" };
	}
	
	@Override
	public void perform(SpiderData entityData)
	{
		
	}
}
