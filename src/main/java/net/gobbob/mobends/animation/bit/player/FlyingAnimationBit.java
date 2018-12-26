package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.PlayerData;

public class FlyingAnimationBit extends AnimationBit<PlayerData>
{

	@Override
	public String[] getActions(PlayerData entityData)
	{
		return new String[] { "flying" };
	}

	@Override
	public void perform(PlayerData entityData)
	{
	}

}
