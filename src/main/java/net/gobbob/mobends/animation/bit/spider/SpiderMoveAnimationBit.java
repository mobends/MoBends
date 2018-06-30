package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class SpiderMoveAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "move" };
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		
	}
}
