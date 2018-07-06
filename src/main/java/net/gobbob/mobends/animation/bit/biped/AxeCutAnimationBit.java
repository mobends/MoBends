package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.Minecraft;

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
		float ticks = DataUpdateHandler.getTicks();
	}
}
