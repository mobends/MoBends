package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.data.EntityData;
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
		data.spiderLeg1.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.spiderLeg2.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.spiderLeg3.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.spiderLeg4.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.spiderLeg5.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.spiderLeg6.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.spiderLeg7.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.spiderLeg8.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		
		float foreLegAngle = 70.0F - ticksInAir * 50.0F;
		data.spiderForeLeg1.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.spiderForeLeg2.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.spiderForeLeg3.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.spiderForeLeg4.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.spiderForeLeg5.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.spiderForeLeg6.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.spiderForeLeg7.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.spiderForeLeg8.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
	}
}
