package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.standard.data.SpiderData;

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
