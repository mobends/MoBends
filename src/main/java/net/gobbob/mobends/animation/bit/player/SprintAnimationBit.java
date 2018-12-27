package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.data.PlayerData;
import net.minecraft.util.math.MathHelper;

public class SprintAnimationBit extends net.gobbob.mobends.animation.bit.biped.SprintAnimationBit<PlayerData>
{
	@Override
	public void perform(PlayerData data)
	{
		super.perform(data);
		
		if (data.getTicksAfterAttack() < 10) {
			data.head.rotation.setSmoothness(0.5F).orientX(MathHelper.wrapDegrees(data.getHeadPitch()))
		  	  									  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()));
		}
	}
}
