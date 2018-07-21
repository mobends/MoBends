package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;

public class AxeCutAnimationBit extends AnimationBit<BipedEntityData>
{
	@Override
	public String[] getActions(BipedEntityData entityData)
	{
		return new String[] { "axe_cut" };
	}

	@Override
	public void perform(BipedEntityData entityData)
	{
		float ticks = DataUpdateHandler.getTicks();
	}
}
