package net.gobbob.mobends.standard.animation.bit.player;

import net.gobbob.mobends.standard.data.PlayerData;
import net.minecraft.util.math.MathHelper;

public class WalkAnimationBit extends net.gobbob.mobends.standard.animation.bit.biped.WalkAnimationBit<PlayerData>
{
	@Override
	public void perform(PlayerData data)
	{
		super.perform(data);
		
		if (data.getTicksAfterAttack() < 10) {
			data.head.rotation.setSmoothness(0.5F).orientX(MathHelper.wrapDegrees(data.headPitch.get()))
		  	  									  .rotateY(MathHelper.wrapDegrees(data.headYaw.get()));
		}
	}
}
