package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.SpiderData;

public class SpiderMoveAnimationBit extends AnimationBit<SpiderData>
{
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return new String[] { "move" };
	}
	
	@Override
	public void perform(SpiderData entityData)
	{
		
	}
}
