package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;

public class AxeCutAnimationBit extends AnimationBit
{
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "axe_cut" };
	}

	@Override
	public void perform(EntityData entityData)
	{
		
	}

}
