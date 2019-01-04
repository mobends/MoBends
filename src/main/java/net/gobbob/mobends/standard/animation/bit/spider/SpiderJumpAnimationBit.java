package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.SpiderData;

public class SpiderJumpAnimationBit extends AnimationBit<SpiderData>
{
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return new String[] { "jump" };
	}
	
	@Override
	public void perform(SpiderData data)
	{
		float ticksInAir = data.getTicksInAir();
		ticksInAir = Math.min(ticksInAir * 0.1F, 1.0F);
		ticksInAir = (float) GUtil.easeInOut(ticksInAir, 3);
		
		float legAngle = -100.0F + ticksInAir * 95.0F;
		float smoothness = 1F;
		data.upperLimbs[0].part.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.upperLimbs[1].part.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.upperLimbs[2].part.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.upperLimbs[3].part.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.upperLimbs[4].part.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.upperLimbs[5].part.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.upperLimbs[6].part.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.upperLimbs[7].part.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		
		float foreLegAngle = 70.0F - ticksInAir * 50.0F;
		data.lowerLimbs[0].part.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.lowerLimbs[1].part.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.lowerLimbs[2].part.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.lowerLimbs[3].part.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.lowerLimbs[4].part.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.lowerLimbs[5].part.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.lowerLimbs[6].part.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.lowerLimbs[7].part.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
	}
}
